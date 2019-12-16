package exec

import (
	"errors"

	"github.com/PlatONnetwork/PlatON-Go/life/utils"
)

var _ ImportResolver = (*NopResolver)(nil)

// NopResolver is a nil WebAssembly module import resolver.
type NopResolver struct{}

func (r *NopResolver) ResolveFunc(module, field string) *FunctionImport {
	panic("func import not allowed")
}

func (r *NopResolver) ResolveGlobal(module, field string) int64 {
	panic("global import not allowed")
}

// RunWithGasLimit runs a WebAssembly modules function denoted by its ID with a specified set
// of parameters for a specified amount of instructions (also known as gas) denoted by `limit`.
// Panics on logical errors.
/**
RunWithGasLimit:
运行由其ID表示的WebAssembly模块功能，并使用一组指定的参数来表示由`limit`表示的指定数量的指令（也称为gas）。 对逻辑错误感到恐慌。
 */
func (vm *VirtualMachine) RunWithGasLimit (entryID, limit int, params ...int64) (int64, error) {
	count := 0

	// Ignite初始化第一个调用帧
	vm.Ignite(entryID, params...)
	/**
	todo 执行 指令
	 */
	for !vm.Exited {

		// 执行指令
		vm.Execute()
		if vm.Delegate != nil {
			vm.Delegate()
			vm.Delegate = nil
		}
		count++

		// 该 limit 其实就是 tx中给的 gas
		//
		// todo 当 for 中出现了gasCount == limit 说明 运行指令需要的gas超过了 tx.Gas了
		if count == limit {
			return -1, errors.New("gas limit exceeded")
		}
	}

	if vm.ExitError != nil {
		return -1, utils.UnifyError(vm.ExitError)
	}
	return vm.ReturnValue, nil

}

// Run runs a WebAssembly modules function denoted by its ID with a specified set
// of parameters.
// Panics on logical errors.
func (vm *VirtualMachine) Run(entryID int, params ...int64) (int64, error) {
	vm.Ignite(entryID, params...)
	for !vm.Exited {
		vm.Execute()
		if vm.Delegate != nil {
			vm.Delegate()
			vm.Delegate = nil
		}
	}

	if vm.ExitError != nil {
		return -1, utils.UnifyError(vm.ExitError)
	}
	return vm.ReturnValue, nil
}

func (vm *VirtualMachine) Stop() (err error) {
	for _, pos := range vm.ExternalParams {
		err = vm.Memory.Free(int(pos))

	}
	memPool.Put(vm.Memory.Memory)
	treePool.PutTree(vm.Memory.tree)
	return err
}
