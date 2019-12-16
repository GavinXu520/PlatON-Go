package compiler

// Gas costs
const (
	GasQuickStep   uint64 = 2
	GasFastestSetp uint64 = 3

	// ...
)

func (c *SSAFunctionCompiler) InsertGasCounters(gp GasPolicy) {
	cfg := c.NewCFGraph()


	// todo 这里是和 编译器相关的了
	// 		遍历编译的 block
	for i, _ := range cfg.Blocks {
		blk := &cfg.Blocks[i]
		totalCost := int64(0)
		// todo 根据block 对应的指令查询出对应的gas计价
		// 		将对应指令的 gas全部叠加起来
		for _, ins := range blk.Code {
			totalCost += gp.GetCost(ins.Op)
			if totalCost < 0 {
				panic("total cost overflow")
			}
		}


		// todo 在计算完所有 当前指令的gas计价总和之后
		// 		再将截止当前为止的gas计价作为指令追加到当前指令之后
		if totalCost != 0 {
			blk.Code = append([]Instr{
				buildInstr(0, "add_gas", []int64{totalCost}, []TyValueID{}),
			}, blk.Code...)
		}
	}

	// todo 将插入了gas的指令进行序列化
	c.Code = cfg.ToInsSeq()
}
