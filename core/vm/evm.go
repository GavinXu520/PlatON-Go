// Copyright 2014 The go-ethereum Authors
// This file is part of the go-ethereum library.
//
// The go-ethereum library is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// The go-ethereum library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with the go-ethereum library. If not, see <http://www.gnu.org/licenses/>.

package vm

import (
	"math/big"
	"strings"
	"sync/atomic"
	"time"

	"github.com/PlatONnetwork/PlatON-Go/x/plugin"

	"github.com/PlatONnetwork/PlatON-Go/common"
	"github.com/PlatONnetwork/PlatON-Go/crypto"
	"github.com/PlatONnetwork/PlatON-Go/params"
)

// emptyCodeHash is used by create to ensure deployment is disallowed to already
// deployed contract addresses (relevant after the account abstraction).
var emptyCodeHash = crypto.Keccak256Hash(nil)

type (
	// CanTransferFunc is the signature of a transfer guard function
	CanTransferFunc func(StateDB, common.Address, *big.Int) bool
	// TransferFunc is the signature of a transfer function
	TransferFunc func(StateDB, common.Address, common.Address, *big.Int)
	// GetHashFunc returns the nth block hash in the blockchain
	// and is used by the BLOCKHASH EVM op code.
	GetHashFunc func(uint64) common.Hash
)

// run runs the given contract and takes care of running precompiles with a fallback to the byte code interpreter.
//
// todo 执行合约， 其中 readOnly 标识符只有从 StaticCall 进来才会有， 表示无状态改变的对合约进行调用
func run(evm *EVM, contract *Contract, input []byte, readOnly bool) ([]byte, error) {
	if contract.CodeAddr != nil {
		precompiles := PrecompiledContractsHomestead

		if p := precompiles[*contract.CodeAddr]; p != nil {
			return RunPrecompiledContract(p, input, contract)
		}

		if p := PlatONPrecompiledContracts[*contract.CodeAddr]; p != nil {
			switch p.(type) {

			case *validatorInnerContract:
				vic := &validatorInnerContract{
					Contract: contract,
					Evm:      evm,
				}
				return RunPrecompiledContract(vic, input, contract)

			case *StakingContract:
				staking := &StakingContract{
					Plugin:   plugin.StakingInstance(),
					Contract: contract,
					Evm:      evm,
				}
				return RunPlatONPrecompiledContract(staking, input, contract)
			case *RestrictingContract:
				restricting := &RestrictingContract{
					Plugin:   plugin.RestrictingInstance(),
					Contract: contract,
					Evm:      evm,
				}
				return RunPlatONPrecompiledContract(restricting, input, contract)
			case *SlashingContract:
				slashing := &SlashingContract{
					Plugin:   plugin.SlashInstance(),
					Contract: contract,
					Evm:      evm,
				}
				return RunPlatONPrecompiledContract(slashing, input, contract)
			case *GovContract:
				govContract := &GovContract{
					Plugin:   plugin.GovPluginInstance(),
					Contract: contract,
					Evm:      evm,
				}
				return RunPlatONPrecompiledContract(govContract, input, contract)
			}
		}

	}

	for _, interpreter := range evm.interpreters {
		if interpreter.CanRun(contract.Code) {
			if evm.interpreter != interpreter {
				// Ensure that the interpreter pointer is set back
				// to its current value upon return.
				//
				// todo 确保在返回时将解 释器指针设置回其当前值。 （干嘛的？ 不懂）
				defer func(i Interpreter) {
					evm.interpreter = i
				}(evm.interpreter)
				evm.interpreter = interpreter
			}

			/**
			todo 这里是具体的执行器指针
				evm 或者 wasm
			*/
			return interpreter.Run(contract, input, readOnly)
		}
	}
	return nil, ErrNoCompatibleInterpreter
}

// Context provides the EVM with auxiliary information. Once provided
// it shouldn't be modified.
type Context struct {
	// CanTransfer returns whether the account contains
	// sufficient ether to transfer the value
	CanTransfer CanTransferFunc
	// Transfer transfers ether from one account to the other
	Transfer TransferFunc
	// GetHash returns the hash corresponding to n
	GetHash GetHashFunc

	// Message information
	// todo 实际上这个就是 msg.from (tx.from)
	Origin   common.Address // Provides information for ORIGIN
	GasPrice *big.Int       // Provides information for GASPRICE

	// Block information
	Coinbase    common.Address // Provides information for COINBASE
	GasLimit    uint64         // Provides information for GASLIMIT
	BlockNumber *big.Int       // Provides information for NUMBER
	Time        *big.Int       // Provides information for TIME
	Difficulty  *big.Int       // Provides information for DIFFICULTY

	BlockHash common.Hash // Only, the value will be available after the current block has been sealed.

	// todo 需要在这里添加选择什么 vm
}

// EVM is the Ethereum Virtual Machine base object and provides
// the necessary tools to run a contract on the given state with
// the provided context. It should be noted that any error
// generated through any of the calls should be considered a
// revert-state-and-consume-all-gas operation, no checks on
// specific errors should ever be performed. The interpreter makes
// sure that any errors generated are to be considered faulty code.
//
// The EVM should never be reused and is not thread safe.
type EVM struct {
	// Context provides auxiliary blockchain related information
	Context
	// StateDB gives access to the underlying state
	StateDB StateDB
	// Depth is the current call stack
	//
	// todo 合约调用栈深度，主要用于合约跨合约时的调用深度，每一个合约执行的时候，都会进入执行器的 Run() 函数，
	// 	进去的时候都会 depth++，在执行完该合约时会 depth--
	depth int

	// chainConfig contains information about the current chain
	chainConfig *params.ChainConfig
	// chain rules contains the chain rules for the current epoch
	chainRules params.Rules
	// virtual machine configuration options used to initialise the
	// evm.
	vmConfig Config
	// global (to this context) ethereum virtual machine
	// used throughout the execution of the tx.
	//
	// 在执行TX期间使用的全局（就此而言）以太坊虚拟机。
	// 所有的执行器
	interpreters []Interpreter
	// todo 当前的执行器
	interpreter Interpreter
	// abort is used to abort the EVM calling operations
	// NOTE: must be set atomically
	//
	// todo 中断执行标识位
	abort int32
	// callGasTemp holds the gas available for the current call. This is needed because the
	// available gas is calculated in gasCall* according to the 63/64 rule and later
	// applied in opCall*.
	//
	// todo 缓存gas消耗的中间变量
	callGasTemp uint64
}

// NewEVM returns a new EVM. The returned EVM is not thread safe and should
// only ever be used *once*.
func NewEVM(ctx Context, statedb StateDB, chainConfig *params.ChainConfig, vmConfig Config) *EVM {
	evm := &EVM{
		Context:      ctx,
		StateDB:      statedb,
		vmConfig:     vmConfig,
		chainConfig:  chainConfig,
		chainRules:   chainConfig.Rules(ctx.BlockNumber),
		interpreters: make([]Interpreter, 0, 1),
	}

	// vmConfig.EVMInterpreter will be used by EVM-C, it won't be checked here
	// as we always want to have the built-in EVM as the failover option.
	// todo: replace the evm to wasm for the interpreter.

	// todo 以前是在 NewEVM 的时候决定 实例化 evm 执行器还是 wasm 执行器
	// 		因为最终，都是交由执行器来做执行 指令码
	if strings.EqualFold("evm", chainConfig.VMInterpreter) {
		evm.interpreters = append(evm.interpreters, NewEVMInterpreter(evm, vmConfig))
	} else {

		// todo 实例化了一个 wasm 执行器
		evm.interpreters = append(evm.interpreters, NewWASMInterpreter(evm, vmConfig))
	}
	evm.interpreter = evm.interpreters[0]
	return evm
}

// Cancel cancels any running EVM operation. This may be called concurrently and
// it's safe to be called multiple times.
func (evm *EVM) Cancel() {
	atomic.StoreInt32(&evm.abort, 1)
}

// Interpreter returns the current interpreter
func (evm *EVM) Interpreter() Interpreter {
	return evm.interpreter
}

// Call executes the contract associated with the addr with the given input as
// parameters. It also handles any necessary value transfer required and takes
// the necessary steps to create accounts and reverses the state in case of an
// execution error or failed value transfer.
//
/**
todo 这个是正常的 call 调用合约
	但是如果是合约调合约的call， 那么也会进来
*/
func (evm *EVM) Call(caller ContractRef, addr common.Address, input []byte, gas uint64, value *big.Int) (ret []byte, leftOverGas uint64, err error) {
	if evm.vmConfig.NoRecursion && evm.depth > 0 {
		return nil, gas, nil
	}

	// Fail if we're trying to execute above the call depth limit
	if evm.depth > int(params.CallCreateDepth) {
		return nil, gas, ErrDepth
	}
	// Fail if we're trying to transfer more than the available balance
	if !evm.Context.CanTransfer(evm.StateDB, caller.Address(), value) {
		return nil, gas, ErrInsufficientBalance
	}

	var (
		to       = AccountRef(addr)
		snapshot = evm.StateDB.Snapshot() // - snapshot.
	)
	if !evm.StateDB.Exist(addr) {
		precompiles := PrecompiledContractsHomestead

		if precompiles[addr] == nil && PlatONPrecompiledContracts[addr] == nil && value.Sign() == 0 {
			// Calling a non existing account, don't do anything, but ping the tracer
			if evm.vmConfig.Debug && evm.depth == 0 {
				evm.vmConfig.Tracer.CaptureStart(caller.Address(), addr, false, input, gas, value)
				evm.vmConfig.Tracer.CaptureEnd(ret, 0, 0, nil)
			}
			return nil, gas, nil
		}
		evm.StateDB.CreateAccount(addr)
	}

	// todo 只有 call 才做了转账
	evm.Transfer(evm.StateDB, caller.Address(), to.Address(), value)

	// Initialise a new contract and set the code that is to be used by the EVM.
	// The contract is a scoped environment for this execution context only.
	//
	/**
	todo 创建一个 合约上下文，非常重要
		因为，当合约调合约的时候也是用这个的。
		A -> B 的时候
		注意： 此时的 evm 还是 同一个evm ，所以里头需要用到 depth
	*/
	contract := NewContract(caller, to, value, gas)

	contract.SetCallCode(&addr, evm.StateDB.GetCodeHash(addr), evm.StateDB.GetCode(addr))
	start := time.Now()

	// Capture the tracer start/end events in debug mode
	if evm.vmConfig.Debug && evm.depth == 0 {
		evm.vmConfig.Tracer.CaptureStart(caller.Address(), addr, false, input, gas, value)

		defer func() { // Lazy evaluation of the parameters
			evm.vmConfig.Tracer.CaptureEnd(ret, gas-contract.Gas, time.Since(start), err)
		}()
	}
	ret, err = run(evm, contract, input, false)

	// When an error was returned by the EVM or when setting the creation code
	// above we revert to the snapshot and consume any gas remaining. Additionally
	// when we're in homestead this also counts for code storage gas errors.
	if err != nil {
		evm.StateDB.RevertToSnapshot(snapshot)
		if err != errExecutionReverted {
			contract.UseGas(contract.Gas)
		}
	}
	return ret, contract.Gas, err
}

// CallCode executes the contract associated with the addr with the given input
// as parameters. It also handles any necessary value transfer required and takes
// the necessary steps to create accounts and reverses the state in case of an
// execution error or failed value transfer.
//
// CallCode differs from Call in the sense that it executes the given address'
// code with the caller as context.
//
/**
todo 注意了， CallCode 其实只会改变发起方的 state


todo  说白了这个就是用来 使用：  被调用方的 code 来修改 发起方的值
*/
func (evm *EVM) CallCode(caller ContractRef, addr common.Address, input []byte, gas uint64, value *big.Int) (ret []byte, leftOverGas uint64, err error) {
	if evm.vmConfig.NoRecursion && evm.depth > 0 {
		return nil, gas, nil
	}

	// Fail if we're trying to execute above the call depth limit
	if evm.depth > int(params.CallCreateDepth) {
		return nil, gas, ErrDepth
	}
	// Fail if we're trying to transfer more than the available balance
	if !evm.CanTransfer(evm.StateDB, caller.Address(), value) {
		return nil, gas, ErrInsufficientBalance
	}

	// todo 可以看得出来，CallCode 并没有做 transfer 操作
	var (
		snapshot = evm.StateDB.Snapshot()

		// todo ###########################
		// todo ###########################
		// todo ###########################
		//
		// todo 且， 使用了 caller 作为 to
		to = AccountRef(caller.Address())
	)
	// initialise a new contract and set the code that is to be used by the
	// EVM. The contract is a scoped environment for this execution context
	// only.
	//
	// 不做转账了，为什么还有 value ？？
	contract := NewContract(caller, to, value, gas)

	// todo 但是 却是用了 被调用合约的 code
	contract.SetCallCode(&addr, evm.StateDB.GetCodeHash(addr), evm.StateDB.GetCode(addr))

	ret, err = run(evm, contract, input, false)
	if err != nil {
		evm.StateDB.RevertToSnapshot(snapshot)
		if err != errExecutionReverted {
			contract.UseGas(contract.Gas)
		}
	}
	return ret, contract.Gas, err
}

// DelegateCall executes the contract associated with the addr with the given input
// as parameters. It reverses the state in case of an execution error.
//
// DelegateCall differs from CallCode in the sense that it executes the given address'
// code with the caller as context and the caller is set to the caller of the caller.
/**
todo 委托调用合约

todo  说白了这个就是用来 使用：  被调用方的 code 来修改 发起方的值
*/
func (evm *EVM) DelegateCall(caller ContractRef, addr common.Address, input []byte, gas uint64) (ret []byte, leftOverGas uint64, err error) {
	if evm.vmConfig.NoRecursion && evm.depth > 0 {
		return nil, gas, nil
	}
	// Fail if we're trying to execute above the call depth limit
	if evm.depth > int(params.CallCreateDepth) {
		return nil, gas, ErrDepth
	}

	// todo 这个，直接不判断 余额了

	// todo  也不做转账
	var (
		snapshot = evm.StateDB.Snapshot()

		// todo ###########################
		// todo ###########################
		// todo ###########################
		//
		// todo 且， 使用了 caller 作为 to
		to = AccountRef(caller.Address())
	)

	// Initialise a new contract and make initialise the delegate values
	// todo ###########################
	// todo ###########################
	// todo ###########################
	//
	// todo 最主要一点是在创建合约上下文的时候， 使用了 AsDelegate()
	contract := NewContract(caller, to, nil, gas).AsDelegate()

	// todo 但是 却也是用了 被调用合约的 code
	contract.SetCallCode(&addr, evm.StateDB.GetCodeHash(addr), evm.StateDB.GetCode(addr))

	ret, err = run(evm, contract, input, false)
	if err != nil {
		evm.StateDB.RevertToSnapshot(snapshot)
		if err != errExecutionReverted {
			contract.UseGas(contract.Gas)
		}
	}
	return ret, contract.Gas, err
}

// StaticCall executes the contract associated with the addr with the given input
// as parameters while disallowing any modifications to the state during the call.
// Opcodes that attempt to perform such modifications will result in exceptions
// instead of performing the modifications.
//
// todo 静态调用
//
// todo STATICCALL是CALL的新变体，它仅允许对其他合同（包括其本身）进行无状态更改的调用
func (evm *EVM) StaticCall(caller ContractRef, addr common.Address, input []byte, gas uint64) (ret []byte, leftOverGas uint64, err error) {
	if evm.vmConfig.NoRecursion && evm.depth > 0 {
		return nil, gas, nil
	}
	// Fail if we're trying to execute above the call depth limit
	if evm.depth > int(params.CallCreateDepth) {
		return nil, gas, ErrDepth
	}

	// todo 不校验 余额

	// todo  不作转账
	var (

		// todo 正常的和call一样 to就是to
		to       = AccountRef(addr)
		snapshot = evm.StateDB.Snapshot()
	)
	// Initialise a new contract and set the code that is to be used by the
	// EVM. The contract is a scoped environment for this execution context
	// only.
	contract := NewContract(caller, to, new(big.Int), gas)
	contract.SetCallCode(&addr, evm.StateDB.GetCodeHash(addr), evm.StateDB.GetCode(addr))

	// When an error was returned by the EVM or when setting the creation code
	// above we revert to the snapshot and consume any gas remaining. Additionally
	// when we're in Homestead this also counts for code storage gas errors.
	ret, err = run(evm, contract, input, true)
	if err != nil {
		evm.StateDB.RevertToSnapshot(snapshot)
		if err != errExecutionReverted {
			contract.UseGas(contract.Gas)
		}
	}
	return ret, contract.Gas, err
}

// create creates a new contract using code as deployment code.
//
// todo 注意：原本 sol 的合约部署时， tx.data 字段中的内容其实是 【三部分】
//
// todo  如：
// 	{
//  "from": "0xbd04d16f09506e80d1fd1fd8d0c79afa49bd9976",
//  "to": null,
//  "gas": "68653", // 30400,
//  "gasPrice": "1", // 10000000000000
//  "data": "0x60606040523415600e57600080fd5b603580601b6000396000f3006060604052600080fd00a165627a7a723058204bf1accefb2526a5077bcdfeaeb8020162814272245a9741cc2fddd89191af1c0029"
//	}
//
// todo data将拆分成 三部分
/**
todo 【部署代码】 这个就是合约的代码
	60606040523415600e57600080fd5b5b603680601c6000396000f300
todo 【合约代码】 合约创建成功之后当它的方法被调用时，运行合约代码
	60606040525b600080fd00
todo 【Auxdata】 （可选）Auxdata是源码的加密指纹，用来验证。这只是数据，永远不会被EVM执行
	a165627a7a723058209747525da0f525f1132dde30c8276ec70c4786d4b08a798eda3c8314bf796cc30029

todo 部署代码有两个主要作用：

todo 1) 运行构造器函数，并设置初始化内存变量（就像合约的拥有者）
todo 2) 计算合约代码，并返回给EVM


todo  Solidity编译器产生的部署代码会从字节码中加载60606040525b600080fd00到内存中，然后将它作为合约代码返回。在这个例子中， ""计算""只 是读取一块数据到内存中
*/
func (evm *EVM) create(caller ContractRef, code []byte, gas uint64, value *big.Int, address common.Address) ([]byte, common.Address, uint64, error) {
	// Depth check execution. Fail if we're trying to execute above the
	// limit.
	if evm.depth > int(params.CallCreateDepth) {
		return nil, common.Address{}, gas, ErrDepth
	}
	if !evm.CanTransfer(evm.StateDB, caller.Address(), value) {
		return nil, common.Address{}, gas, ErrInsufficientBalance
	}
	nonce := evm.StateDB.GetNonce(caller.Address())
	evm.StateDB.SetNonce(caller.Address(), nonce+1)

	// Ensure there's no existing contract already at the designated address
	contractHash := evm.StateDB.GetCodeHash(address)
	if evm.StateDB.GetNonce(address) != 0 || (contractHash != (common.Hash{}) && contractHash != emptyCodeHash) {
		return nil, common.Address{}, 0, ErrContractAddressCollision
	}
	// Create a new account on the state
	snapshot := evm.StateDB.Snapshot()
	evm.StateDB.CreateAccount(address)
	evm.StateDB.SetNonce(address, 1)

	// todo 将初始的Ether基金从调用者转到新合约中
	evm.Transfer(evm.StateDB, caller.Address(), address, value)

	// initialise a new contract and set the code that is to be used by the
	// EVM. The contract is a scoped environment for this execution context
	// only.
	//
	// todo 设置输入数据为合约的部署代码，然后使用EVM来执行。ret变量是返回的合约代码
	contract := NewContract(caller, AccountRef(address), value, gas)
	contract.SetCallCode(&address, crypto.Keccak256Hash(code), code)

	if evm.vmConfig.NoRecursion && evm.depth > 0 {
		return nil, address, gas, nil
	}

	if evm.vmConfig.Debug && evm.depth == 0 {
		evm.vmConfig.Tracer.CaptureStart(caller.Address(), address, true, code, gas, value)
	}
	start := time.Now()

	// todo 注意了 创建 contract 的时候 input为 nil
	//
	// todo 其中 ret 就是返回的 合约code
	ret, err := run(evm, contract, nil, false)

	// check whether the max code size has been exceeded
	maxCodeSizeExceeded := len(ret) > params.MaxCodeSize
	// if the contract creation ran successfully and no errors were returned
	// calculate the gas required to store the code. If the code could not
	// be stored due to not enough gas set an error and let it be handled
	// by the error checking condition below.
	if err == nil && !maxCodeSizeExceeded {
		createDataGas := uint64(len(ret)) * params.CreateDataGas
		if contract.UseGas(createDataGas) {
			evm.StateDB.SetCode(address, ret)
		} else {
			err = ErrCodeStoreOutOfGas
		}
	}

	// When an error was returned by the EVM or when setting the creation code
	// above we revert to the snapshot and consume any gas remaining. Additionally
	// when we're in homestead this also counts for code storage gas errors.
	if maxCodeSizeExceeded || (err != nil && err != ErrCodeStoreOutOfGas) {
		evm.StateDB.RevertToSnapshot(snapshot)
		if err != errExecutionReverted {
			contract.UseGas(contract.Gas)
		}
	}
	// Assign err if contract code size exceeds the max while the err is still empty.
	if maxCodeSizeExceeded && err == nil {
		err = errMaxCodeSizeExceeded
	}
	if evm.vmConfig.Debug && evm.depth == 0 {
		evm.vmConfig.Tracer.CaptureEnd(ret, gas-contract.Gas, time.Since(start), err)
	}
	return ret, address, contract.Gas, err

}

// Create creates a new contract using code as deployment code.
func (evm *EVM) Create(caller ContractRef, code []byte, gas uint64, value *big.Int) (ret []byte, contractAddr common.Address, leftOverGas uint64, err error) {
	contractAddr = crypto.CreateAddress(caller.Address(), evm.StateDB.GetNonce(caller.Address()))

	// todo 这里的 code 就是 tx.data
	return evm.create(caller, code, gas, value, contractAddr)
}

// Create2 creates a new contract using code as deployment code.
//
// The different between Create2 with Create is Create2 uses sha3(0xff ++ msg.sender ++ salt ++ sha3(init_code))[12:]
// instead of the usual sender-and-nonce-hash as the address where the contract is initialized at.
//
/**


todo Create2与Create2之间的区别在于

todo Create2使用sha3（0xff ++ msg.sender ++盐++ sha3（init_code））[12：]代替了通常的 sender + nonce 作为初始化合同的地址的方式.
*/
func (evm *EVM) Create2(caller ContractRef, code []byte, gas uint64, endowment *big.Int, salt *big.Int) (ret []byte, contractAddr common.Address, leftOverGas uint64, err error) {
	contractAddr = crypto.CreateAddress2(caller.Address(), common.BigToHash(salt), code)
	return evm.create(caller, code, gas, endowment, contractAddr)
}

// ChainConfig returns the environment's chain configuration
func (evm *EVM) ChainConfig() *params.ChainConfig { return evm.chainConfig }

func (evm *EVM) GetStateDB() StateDB {
	return evm.StateDB
}

func (evm *EVM) GetEvm() *EVM {
	return evm
}

func (evm *EVM) GetConfig() Config {
	return evm.vmConfig
}
