package compiler

/**
gas规则器，用于对指令插入gas
 */
type GasPolicy interface {
	GetCost(key string) int64
}

type SimpleGasPolicy struct {
	GasPerInstruction int64
}

func (p *SimpleGasPolicy) GetCost(key string) int64 {
	return p.GasPerInstruction
}
