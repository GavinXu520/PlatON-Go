package compiler

import (
	"bytes"
	"encoding/binary"
	"runtime"

	//"fmt"
	"github.com/go-interpreter/wagon/disasm"
	"github.com/go-interpreter/wagon/wasm"
	//"github.com/go-interpreter/wagon/validate"
	"github.com/PlatONnetwork/PlatON-Go/life/compiler/opcodes"
	"github.com/PlatONnetwork/PlatON-Go/life/utils"
	"github.com/go-interpreter/wagon/wasm/leb128"
)

type Module struct {
	Base          *wasm.Module
	FunctionNames map[int]string
}

type InterpreterCode struct {
	NumRegs    int
	NumParams  int
	NumLocals  int
	NumReturns int
	Bytes      []byte
	JITInfo    interface{}
	JITDone    bool
}


/**
todo 根据 code 构造出 module
 */
func LoadModule(raw []byte) (*Module, error) {

	/**
	 todo 重点就是这个 reader
	 */
	reader := bytes.NewReader(raw)


	/**
	TODO 注意 这个就是调用wagon 的根据 code 解出 module
	 */
	m, err := wasm.ReadModule(reader, nil)
	if err != nil {
		runtime.GC()
		return nil, err
	}

	/*err = validate.VerifyModule(m)
	if err != nil {
		return nil, err
	}*/

	functionNames := make(map[int]string)

	for _, sec := range m.Customs {
		if sec.Name == "name" {
			r := bytes.NewReader(sec.RawSection.Bytes)
			for {
				ty, err := leb128.ReadVarUint32(r)
				if err != nil || ty != 1 {
					break
				}
				// 使用 leb128 的编码
				payloadLen, err := leb128.ReadVarUint32(r)
				if err != nil {
					panic(err)
				}
				data := make([]byte, int(payloadLen))
				n, err := r.Read(data)
				if err != nil {
					panic(err)
				}
				if n != len(data) {
					panic("len mismatch")
				}
				{
					r := bytes.NewReader(data)
					for {
						count, err := leb128.ReadVarUint32(r)
						if err != nil {
							break
						}
						for i := 0; i < int(count); i++ {
							index, err := leb128.ReadVarUint32(r)
							if err != nil {
								panic(err)
							}
							nameLen, err := leb128.ReadVarUint32(r)
							if err != nil {
								panic(err)
							}
							name := make([]byte, int(nameLen))
							n, err := r.Read(name)
							if err != nil {
								panic(err)
							}
							if n != len(name) {
								panic("len mismatch")
							}

							/**
							todo 解出 funcName
							 */
							functionNames[int(index)] = string(name)
							//fmt.Printf("%d -> %s\n", int(index), string(name))
						}
					}
				}
			}
			//fmt.Printf("%d function names written\n", len(functionNames))
		}
	}

	return &Module{

		// 真实的 module
		Base:          m,
		// module中所有 funcName
		FunctionNames: functionNames,
	}, nil
}
// gp: gas规则器, 目前传入为 nil
func (m *Module) CompileForInterpreter(gp GasPolicy) (_retCode []InterpreterCode, retErr error) {

	// todo 处理 panic， 可以的这种方式
	defer utils.CatchPanic(&retErr)

	/**
	todo 最终返回这个鸟东西
	 */
	ret := make([]InterpreterCode, 0)
	importTypeIDs := make([]int, 0)

	if m.Base.Import != nil {
		for i := 0; i < len(m.Base.Import.Entries); i++ {
			e := &m.Base.Import.Entries[i]
			if e.Type.Kind() != wasm.ExternalFunction {
				continue
			}
			tyID := e.Type.(wasm.FuncImport).Type
			ty := &m.Base.Types.Entries[int(tyID)]

			buf := &bytes.Buffer{}

			binary.Write(buf, binary.LittleEndian, uint32(1)) // value ID
			binary.Write(buf, binary.LittleEndian, opcodes.InvokeImport)
			binary.Write(buf, binary.LittleEndian, uint32(i))

			binary.Write(buf, binary.LittleEndian, uint32(0))
			if len(ty.ReturnTypes) != 0 {
				binary.Write(buf, binary.LittleEndian, opcodes.ReturnValue)
				binary.Write(buf, binary.LittleEndian, uint32(1))
			} else {
				binary.Write(buf, binary.LittleEndian, opcodes.ReturnVoid)
			}

			code := buf.Bytes()


			// todo 先采集所有 import 的func
			ret = append(ret, InterpreterCode{
				NumRegs:    2,
				NumParams:  len(ty.ParamTypes),
				NumLocals:  0,
				NumReturns: len(ty.ReturnTypes),
				Bytes:      code,
			})
			importTypeIDs = append(importTypeIDs, int(tyID))
		}
	}

	numFuncImports := len(ret)
	ret = append(ret, make([]InterpreterCode, len(m.Base.FunctionIndexSpace))...)


	// 再遍历当前module中所有的 funcs
	for i, f := range m.Base.FunctionIndexSpace {
		//fmt.Printf("Compiling function %d (%+v) with %d locals\n", i, f.Sig, len(f.Body.Locals))
		// 分解给定的func。 它还将 func的父module作为参数来查找fn引用的任何其他函数。
		d, err := disasm.Disassemble(f, m.Base)
		if err != nil {
			panic(err)
		}

		/**
		todo 实例化一个 编译器

		todo 每个 func 都对各自的 compiler <因为 function的的语法结构不一样， 需要各自单独处理>
		 */
		compiler := NewSSAFunctionCompiler(m.Base, d)

		// 给 compiler 加入
		compiler.CallIndexOffset = numFuncImports
		compiler.Compile(importTypeIDs)

		/**
		todo ##########################
		todo ##########################
		todo ##########################

		todo 这里就是插入 gas

		todo ##########################
		todo ##########################
		todo ##########################
		 */
		if gp != nil {
			compiler.InsertGasCounters(gp)
		}
		//fmt.Println(compiler.Code)
		//fmt.Printf("%+v\n", compiler.NewCFGraph())
		numRegs := compiler.RegAlloc()
		//fmt.Println(compiler.Code)
		numLocals := 0
		for _, v := range f.Body.Locals {
			numLocals += int(v.Count)
		}
		ret[numFuncImports+i] = InterpreterCode{
			NumRegs:    numRegs,
			NumParams:  len(f.Sig.ParamTypes),
			NumLocals:  numLocals,
			NumReturns: len(f.Sig.ReturnTypes),
			Bytes:      compiler.Serialize(),
		}
	}

	return ret, nil
}
