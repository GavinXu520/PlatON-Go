package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"github.com/PlatONnetwork/PlatON-Go/common"
	"github.com/PlatONnetwork/PlatON-Go/common/hexutil"
	"github.com/PlatONnetwork/PlatON-Go/core/types"
	"github.com/PlatONnetwork/PlatON-Go/crypto"
	"github.com/PlatONnetwork/PlatON-Go/ethclient"
	"math/big"
	"time"
)

//var account = flag.String("account", "", "bench of account")

var manager = flag.String("manager", "", "manager account")

var rpc = flag.String("rpc", "", "rpc")

var amount = flag.String("amount", "20000000000000000000000", "transfer amount")

var gasLimit = flag.Uint64("gas", 50000, "gas limit")
var gasPrice = flag.Int64("price", 10000000000000, "gas price")

var chainId = flag.Int64("id", 101, "chain id")

var parallel = flag.Int("parallel", 100, "parallel send tx")

var skip = flag.Int("skip", 0, "skip account")

type keystore struct {
	PrivateKey string `json:"private_key"`
	Address    string `json:"address"`
}

func main() {
	flag.Parse()

	priKey := crypto.HexMustToECDSA(*manager)
	managerAddr := crypto.PubkeyToAddress(priKey.PublicKey)
	keys, err := parseKeyStore()
	if err != nil {
		fmt.Println("parse account json failed", err)
		return
	}

	client, err := connectServer()

	if err != nil {
		fmt.Println("connect server failed", err)
		return
	}
	ctx := context.Background()
	chainId := big.NewInt(*chainId)

	balance, _ := client.BalanceAt(ctx, managerAddr, nil)

	fmt.Println("manager", managerAddr.String(), "chainId", chainId, "balance", balance.String())

	value, _ := new(big.Int).SetString(*amount, 10)

	sendTransaction := func(nonce uint64, addrs []common.Address) error {
		var lastHash common.Hash
		for _, addr := range addrs {
			tx := types.NewTransaction(nonce, addr, value, *gasLimit, big.NewInt(*gasPrice), nil)

			signer := types.NewEIP155Signer(chainId)
			signTx, err := types.SignTx(tx, signer, priKey)
			if err != nil {
				return fmt.Errorf("sign tx failed addr:%s err:%v", addr.String(), err)
			}

			err = client.SendTransaction(ctx, signTx)
			if err != nil {
				return fmt.Errorf("send transaction failed addr:%s err:%v", addr.String(), err)
			}
			lastHash = signTx.Hash()
			nonce++
		}
		for {
			_, err := client.TransactionReceipt(ctx, lastHash)
			if err != nil {
				time.Sleep(500 * time.Millisecond)
				continue
			}
			for _, addr := range addrs {
				fmt.Println("transfer success", addr.String())
			}
			break
		}
		return nil
	}

	count := 0
	addrs := make([]common.Address, 0)
	for i, key := range keys {
		if i < *skip {
			continue
		}
		addrs = append(addrs, common.BytesToAddress(hexutil.MustDecode(key.Address)))
		if count == *parallel || i+1 == len(keys) {
			nonce, err := client.NonceAt(ctx, managerAddr, nil)
			if err != nil {
				fmt.Println("get nonce failed", err, "enable skip", i-count)
				return
			}
			if err := sendTransaction(nonce, addrs); err != nil {
				fmt.Println("send transaction failed", err, "enable skip", i-count)
				return
			}
			count = 0
			addrs = make([]common.Address, 0)
		}
		count++

	}

	balance, _ = client.BalanceAt(ctx, managerAddr, nil)
	fmt.Println("manager", managerAddr.String(), "chainId", chainId, "balance", balance.String())

	fmt.Println("check account balance")
	for _, key := range keys {
		addr := common.BytesToAddress(hexutil.MustDecode(key.Address))

		balance, err := client.BalanceAt(ctx, addr, nil)
		if err != nil {
			fmt.Println("get balance failed", key.Address, err)
		}
		if balance.Cmp(value) < 0 {
			fmt.Println("balance too small", key.Address, "balance", balance.String())
		}

	}
	fmt.Println("check account balance success")

}

func parseKeyStore() ([]keystore, error) {
	//buf, err := ioutil.ReadFile(*account)
	//if err != nil {
	//	return nil, err
	//}
	buf := []byte(accounts)
	var keys []keystore
	if err := json.Unmarshal(buf, &keys); err != nil {
		return nil, err
	}
	return keys, nil
}

func connectServer() (*ethclient.Client, error) {
	client, err := ethclient.Dial(*rpc)
	if err != nil {
		return nil, err
	}
	return client, nil
}

//var accounts = `[
//  {
//    "private_key": "f7b60fce6ae799ed1fe1e698d384bc59c9e5d36784dc69c6aacb61148bb9d193",
//    "address": "0x7E4980211c513aE0f5ABDCfcD509Ae7B27b862DF"
//  },
//  {
//    "private_key": "46f26eab2030510e76fe636dec5a6ba8a0fd41e052167ad9929b44eaaae25172",
//    "address": "0x24b10d266b670fB5b07549ac36463FD8d2A1387a"
//  }]`
