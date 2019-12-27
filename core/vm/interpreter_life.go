package vm

import (
	"bytes"
	"encoding/binary"
	"errors"
	"fmt"
	"math/big"
	"reflect"
	"runtime"
	"strings"

	"github.com/PlatONnetwork/PlatON-Go/common"
	"github.com/PlatONnetwork/PlatON-Go/common/math"
	"github.com/PlatONnetwork/PlatON-Go/core/lru"
	"github.com/PlatONnetwork/PlatON-Go/life/utils"
	"github.com/PlatONnetwork/PlatON-Go/log"
	"github.com/PlatONnetwork/PlatON-Go/rlp"

	"github.com/PlatONnetwork/PlatON-Go/life/exec"
	"github.com/PlatONnetwork/PlatON-Go/life/resolver"
)

var (
	errReturnInvalidRlpFormat   = errors.New("interpreter_life: invalid rlp format.")
	errReturnInsufficientParams = errors.New("interpreter_life: invalid input. ele must greater than 2")
	errReturnInvalidAbi         = errors.New("interpreter_life: invalid abi, encoded fail.")
)

const (
	CALL_CANTRACT_FLAG = 9
)

/**
默认的 wasm 虚机配置项
*/
var DEFAULT_VM_CONFIG = exec.VMConfig{
	// 是否 即时编译 标识位, 默认为 false： 否
	EnableJIT: false,

	// 默认的 内存页数， 16
	DefaultMemoryPages: exec.DefaultMemoryPages,
	// 默认的  动态内存页数， 16
	DynamicMemoryPages: exec.DynamicMemoryPages,
}

// WASMInterpreter represents an WASM interpreter
type WASMInterpreter struct {
	evm         *EVM
	cfg         Config
	wasmStateDB *WasmStateDB
	WasmLogger  log.Logger
	resolver    exec.ImportResolver
	returnData  []byte // todo 这个字段其实没用
}

// NewWASMInterpreter returns a new instance of the Interpreter
func NewWASMInterpreter(evm *EVM, cfg Config) *WASMInterpreter {

	/**
	todo 封装了一层 wasm 的state
	*/
	wasmStateDB := &WasmStateDB{
		StateDB: evm.StateDB,
		evm:     evm,
		cfg:     &cfg,
	}

	// todo 实例化  wasm 的执行器
	return &WASMInterpreter{
		evm: evm,
		cfg: cfg,

		// todo 实例化了 wasm 自己使用的 log实例，区分开全局的log，不希望造成阻塞
		WasmLogger:  NewWasmLogger(cfg, log.WasmRoot()),
		wasmStateDB: wasmStateDB,

		/**
		todo 实例化 wasm 的解析器

		0x01 是clang的解释器
		*/
		resolver: resolver.NewResolver(0x01),
	}
}

// Run loops and evaluates the contract's code with the given input data and returns.
// the return byte-slice and an error if one occurred
//
// It's important to note that any errors returned by the interpreter should be
// considered a revert-and-consume-all-gas operations except for
// errExecutionReverted which means revert-and-keep-gas-lfet.
/**
Run循环并使用给定的输入数据评估 contrct的代码并返回.
返回字节切片，如果发生错误则返回错误.


重要的是要注意，解释器返回的任何错误都应被视为“还原并消耗所有气体”操作，但errExecutionReverted除外，这意味着还原并保持“气体销”.
*/
func (in *WASMInterpreter) Run(contract *Contract, input []byte, readOnly bool) (ret []byte, err error) {

	// todo 在wasm 中没用 readOnly， 因为 还没用到 staticCall 指令

	/**
	todo 捕获内部 panic
	*/
	defer func() {
		if er := recover(); er != nil {
			ret, err = nil, fmt.Errorf("VM execute fail：%v", er)
		}
	}()

	// todo depth 是指 合约调合约时的调用栈 深度
	in.evm.depth++
	defer func() {
		in.evm.depth--

		// todo evm.depth == 0 表示，vm调用结束了
		if in.evm.depth == 0 {
			logger, ok := in.WasmLogger.(*WasmLogger)

			if ok {
				// 清空掉  logger
				logger.Flush()
			}
		}
	}()

	// 先判断下 wasm 合约的 code
	// todo 这是在 Call() 中调用了  SetCallCode() 设置的
	// 		这时候的 Code 还只是 rlp 的 []byte
	if len(contract.Code) == 0 {
		return nil, nil
	}

	// 将 Code 解出来
	// todo 注意 life 版本的 code 中包含两部分， code 和 abi
	//    格式：  rlpData=RLP([txType][code][abi])
	_, abi, code, er := parseRlpData(contract.Code)
	if er != nil {
		return nil, er
	}

	/**
	todo 创建 虚机上下文
	*/
	context := &exec.VMContext{
		/**、。
		拿，默认的 wasm 配置项
		*/
		Config: DEFAULT_VM_CONFIG,

		// 获取当前 contractAddr
		Addr: contract.Address(),

		// 该Gas其实就是 tx中给的 gas
		GasLimit: contract.Gas,

		// 创建 WASM StateDB
		StateDB: NewWasmStateDB(in.wasmStateDB, contract),

		// 使用wasm logger
		Log: in.WasmLogger,
	}

	// todo 一个空的 life VM 实例
	var lvm *exec.VirtualMachine
	// todo  一个 wasm 的module实例 <具备 lru 特性的 wasm module>
	var module *lru.WasmModule

	// 从 cache 拿出一个可以复用的 module 实例 <当前 contract Addr 的>
	module, ok := lru.WasmCache().Get(contract.Address())

	// todo 如果 lru 和 db 中都没拿到 module 的话
	// 	则，新建一个该 contract Addr 的module
	if !ok {
		module = &lru.WasmModule{}
		/**
		todo 根据 code 解出 对用的 func  和 module
		*/
		module.Module, module.FunctionCode, err = exec.ParseModuleAndFunc(code, nil)
		if err != nil {
			return nil, err
		}

		// 将 新建的 module 加入到 lru 中
		lru.WasmCache().Add(contract.Address(), module)
	}

	/**
	todo ############################
	todo ############################
	todo ############################

	TODO 根据 module实例化 lifevm

	todo ############################
	todo ############################
	todo ############################
	*/
	lvm, err = exec.NewVirtualMachineWithModule(module.Module, module.FunctionCode, context, in.resolver, nil)
	if err != nil {
		return nil, err
	}
	defer func() {
		lvm.Stop()
	}()

	// todo 将 tx.data 的内容 赋值给 contract 执行上下文
	contract.Input = input
	var (

		// 本地调用的 func Name
		funcName   string
		txType     int
		params     []int64
		returnType string
	)

	/**
	todo 这里必须要注意： 可以翻看 evm.Create() 和 evm.Call() 的区别
		可以知道， Create() 中的 input就是个 nil，即： 部署合约的时候 input就是 nil
	*/
	if input == nil {
		funcName = "init" // init function.
	} else {
		// parse input.

		// 否则，需要通过根据 abi 解析 input，得到 funcName和params
		// todo 注意了，这里的txType 和 contract.Code 中的 txType 不是同一个东西
		txType, funcName, params, returnType, err = parseInputFromAbi(lvm, input, abi)
		if err != nil {
			if err == errReturnInsufficientParams && txType == 0 { // transfer to contract address.
				return nil, nil
			}
			return nil, err
		}
		if txType == 0 {
			return nil, nil
		}
	}

	// todo 从当前 lvm的 当前 module 中加载出对应的 导出内容
	// 		所谓导出的内容就类似与 js 中的 export module 中可供外部使用的 东东
	// 		也就是说，这个就是 这个module中可以用的东西
	//   这里根据 funcName 获取到其在 wasm 中对应的 entryId，以供调用
	entryID, ok := lvm.GetFunctionExport(funcName)
	if !ok {
		return nil, fmt.Errorf("entryId not found.")
	}

	/**
	TODO 这里就是真正 run wasm 的入口了
	*/
	res, err := lvm.RunWithGasLimit(entryID, int(context.GasLimit), params...)
	if err != nil {
		fmt.Println("throw exception:", err.Error())
		return nil, err
	}

	// 整理执行完指令之后的gas
	if contract.Gas > context.GasUsed {
		contract.Gas = contract.Gas - context.GasUsed
	} else {
		return nil, fmt.Errorf("out of gas.")
	}

	// 如果是 部署合约，则返回部署完之后的 Code
	if input == nil {
		return contract.Code, nil
	}

	// todo: more type need to be completed
	//
	// 否则，根据之前解析 input返回的 returnType，处理 return 类型
	switch returnType {
	case "void", "int8", "int", "int32", "int64":

		// 如果本次 tx的txType是Call
		if txType == CALL_CANTRACT_FLAG {
			return utils.Int64ToBytes(res), nil
		}
		bigRes := new(big.Int)
		bigRes.SetInt64(res)
		finalRes := utils.Align32Bytes(math.U256(bigRes).Bytes())
		return finalRes, nil
	case "uint8", "uint16", "uint32", "uint64":
		if txType == CALL_CANTRACT_FLAG {
			return utils.Uint64ToBytes(uint64(res)), nil
		}
		finalRes := utils.Align32Bytes(utils.Uint64ToBytes((uint64(res))))
		return finalRes, nil
	case "string":
		returnBytes := make([]byte, 0)
		copyData := lvm.Memory.Memory[res:]
		for _, v := range copyData {
			if v == 0 {
				break
			}
			returnBytes = append(returnBytes, v)
		}
		if txType == CALL_CANTRACT_FLAG {
			return returnBytes, nil
		}
		strHash := common.BytesToHash(common.Int32ToBytes(32))
		sizeHash := common.BytesToHash(common.Int64ToBytes(int64((len(returnBytes)))))
		var dataRealSize = len(returnBytes)
		if (dataRealSize % 32) != 0 {
			dataRealSize = dataRealSize + (32 - (dataRealSize % 32))
		}
		dataByt := make([]byte, dataRealSize)
		copy(dataByt[0:], returnBytes)

		finalData := make([]byte, 0)
		finalData = append(finalData, strHash.Bytes()...)
		finalData = append(finalData, sizeHash.Bytes()...)
		finalData = append(finalData, dataByt...)

		//fmt.Println("CallReturn:", string(returnBytes))
		return finalData, nil
	}
	return nil, nil
}

// CanRun tells if the contract, passed as an argument, can be run
// by the current interpreter
func (in *WASMInterpreter) CanRun(code []byte) bool {
	return true
}

// parse input(payload)
//
// todo 根据 abi 解析出 input 中，本次调用的 funcName 和 params 等操作
func parseInputFromAbi(vm *exec.VirtualMachine, input []byte, abi []byte) (txType int, funcName string, params []int64, returnType string, err error) {
	if input == nil || len(input) <= 1 {
		return -1, "", nil, "", fmt.Errorf("invalid input.")
	}
	// [txType][funcName][args1][args2]
	// rlp decode

	// todo 注意，先用指针取值，再用反射取值，和 `parseRlpData` 的做法一样

	//  todo 实例化一个 万能类型指针
	ptr := new(interface{})
	// todo 将 input 解析出来
	err = rlp.Decode(bytes.NewReader(input), &ptr)
	if err != nil {
		return -1, "", nil, "", err
	}

	// todo 根据反射获取出对应的值
	rlpList := reflect.ValueOf(ptr).Elem().Interface()

	// 继续将反射获取的东西, 进行强转断言
	if _, ok := rlpList.([]interface{}); !ok {
		return -1, "", nil, "", errReturnInvalidRlpFormat
	}

	// todo 继续将反射获取的东西，进行强转, 得到对应的list
	iRlpList := rlpList.([]interface{})

	// 如果 len < 2 就说明该 tx是有问题的， 因为至少调用一个 无参 func 时，应该是 txType, FuncName 两个元素
	if len(iRlpList) < 2 {
		if len(iRlpList) != 0 {
			if v, ok := iRlpList[0].([]byte); ok {
				txType = int(common.BytesToInt64(v))
			}
		} else {
			txType = -1
		}
		return txType, "", nil, "", errReturnInsufficientParams
	}

	wasmabi := new(utils.WasmAbi)

	// 将abi由 json -> struct
	err = wasmabi.FromJson(abi)
	if err != nil {
		return -1, "", nil, "", errReturnInvalidAbi
	}

	params = make([]int64, 0)

	// 解出 txType
	if v, ok := iRlpList[0].([]byte); ok {
		txType = int(common.BytesToInt64(v))
	}

	// 解出 funcName
	if v, ok := iRlpList[1].([]byte); ok {
		funcName = string(v)
	}

	var args []utils.InputParam

	// 遍历 abi
	for _, v := range wasmabi.AbiArr {

		// todo 从 abi 中找到对应本次调用的 funcName
		//     收集 input params 和 output params
		if strings.EqualFold(funcName, v.Name) && strings.EqualFold(v.Type, "function") {
			args = v.Inputs
			if len(v.Outputs) != 0 {
				returnType = v.Outputs[0].Type
			} else {
				returnType = "void"
			}
			break
		}
	}

	// todo 获取本次调用中的 所有入参
	argsRlp := iRlpList[2:]

	// todo 如果本次入参的长度和 abi的input params的长度不一致，
	// 		则，抛异常
	if len(args) != len(argsRlp) {
		return -1, "", nil, returnType, fmt.Errorf("invalid input or invalid abi.")
	}
	// uint64 uint32  uint16 uint8 int64 int32  int16 int8 float32 float64 string void

	// todo 根据 abi 的input params 逐个解析本次调用的入参
	for i, v := range args {
		bts := argsRlp[i].([]byte)
		switch v.Type {
		case "string":
			pos := resolver.MallocString(vm, string(bts))
			params = append(params, pos)
		case "int8":
			params = append(params, int64(bts[0]))
		case "int16":
			params = append(params, int64(binary.BigEndian.Uint16(bts)))
		case "int32", "int":
			params = append(params, int64(binary.BigEndian.Uint32(bts)))
		case "int64":
			params = append(params, int64(binary.BigEndian.Uint64(bts)))
		case "uint8":
			params = append(params, int64(bts[0]))
		case "uint32", "uint":
			params = append(params, int64(binary.BigEndian.Uint32(bts)))
		case "uint64":
			params = append(params, int64(binary.BigEndian.Uint64(bts)))
		case "bool":
			params = append(params, int64(bts[0]))
		}
	}

	// 将本次调用的 交易类型、被调用的funcName、入参、返参
	return txType, funcName, params, returnType, nil
}

// rlpData=RLP([txType][code][abi])
//
// todo 解出 存储在db的 code中内容， 组成为： txType,code,abi
func parseRlpData(rlpData []byte) (int64, []byte, []byte, error) {

	// todo 注意，先用指针取值，再用反射取值，和 `parseInputFromAbi` 的做法一样
	ptr := new(interface{})
	err := rlp.Decode(bytes.NewReader(rlpData), &ptr)
	if err != nil {
		return -1, nil, nil, err
	}

	// 取回 list
	rlpList := reflect.ValueOf(ptr).Elem().Interface()

	if _, ok := rlpList.([]interface{}); !ok {
		return -1, nil, nil, fmt.Errorf("invalid rlp format.")
	}

	iRlpList := rlpList.([]interface{})

	// 必须是 3个
	if len(iRlpList) <= 2 {
		return -1, nil, nil, fmt.Errorf("invalid input. ele must greater than 2")
	}
	var (
		// todo 这三个就是要做 反rlp 的内容
		txType int64
		code   []byte
		abi    []byte
	)

	// todo 强转出 []byte，并分别解出 txType， code， abi
	if v, ok := iRlpList[0].([]byte); ok {
		//
		txType = utils.BytesToInt64(v)
	}
	if v, ok := iRlpList[1].([]byte); ok {
		code = v
		//fmt.Println("dstCode: ", common.Bytes2Hex(code))
	}
	if v, ok := iRlpList[2].([]byte); ok {
		abi = v
		//fmt.Println("dstAbi:", common.Bytes2Hex(abi))
	}
	return txType, abi, code, nil
}

func stack() string {
	var buf [2 << 10]byte
	return string(buf[:runtime.Stack(buf[:], true)])
}
