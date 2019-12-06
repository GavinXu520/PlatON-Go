import math
import time

import pytest
import allure
from dacite import from_dict
from common.key import get_pub_key, mock_duplicate_sign
from common.log import log
from client_sdk_python import Web3
from decimal import Decimal
from tests.conftest import get_clients_noconsensus
from tests.lib import (EconomicConfig,
                       Genesis,
                       check_node_in_list,
                       assert_code,
                       von_amount,
                       get_governable_parameter_value,
                       get_pledge_list, HexBytes,
                       wait_block_number)


@pytest.mark.P0
def test_IT_IA_002_to_007(new_genesis_env):
    """
    IT_IA_002:链初始化-查看token发行总量账户初始值
    IT_IA_003:链初始化-查看platON基金会账户初始值
    IT_IA_004:链初始化-查看激励池账户
    IT_IA_005:链初始化-查看剩余总账户
    IT_IA_006:链初始化-查看锁仓账户余额
    IT_IA_007:链初始化-查看质押账户余额
    :return:验证链初始化后token各内置账户初始值
    """
    # Initialization genesis file Initial amount
    node_count = len(new_genesis_env.consensus_node_list)
    default_pledge_amount = Web3.toWei(node_count * 1500000, 'ether')
    node = new_genesis_env.get_rand_node()
    community_amount = default_pledge_amount + 259096239000000000000000000 + 62215742000000000000000000
    genesis = from_dict(data_class=Genesis, data=new_genesis_env.genesis_config)
    genesis.economicModel.innerAcc.cdfBalance = community_amount
    surplus_amount = str(EconomicConfig.TOKEN_TOTAL - community_amount - 200000000000000000000000000)
    genesis.alloc = {
        "1000000000000000000000000000000000000003": {
            "balance": "200000000000000000000000000"
        },
        "0x2e95E3ce0a54951eB9A99152A6d5827872dFB4FD": {
            "balance": surplus_amount
        }
    }
    new_file = new_genesis_env.cfg.env_tmp + "/genesis.json"
    genesis.to_file(new_file)
    new_genesis_env.deploy_all(new_file)

    # Verify the amount of each built-in account
    foundation_louckup = node.eth.getBalance(EconomicConfig.FOUNDATION_LOCKUP_ADDRESS)
    log.info('Initial lock up contract address： {} amount：{}'.format(EconomicConfig.FOUNDATION_LOCKUP_ADDRESS,
                                                                     foundation_louckup))
    incentive_pool = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info('Incentive pool address：{} amount：{}'.format(EconomicConfig.INCENTIVEPOOL_ADDRESS, incentive_pool))
    staking = node.eth.getBalance(EconomicConfig.STAKING_ADDRESS)
    log.info('Address of pledge contract：{} amount：{}'.format(EconomicConfig.STAKING_ADDRESS, staking))
    foundation = node.eth.getBalance(node.web3.toChecksumAddress(EconomicConfig.FOUNDATION_ADDRESS))
    log.info('PlatON Foundation address：{} amount：{}'.format(EconomicConfig.FOUNDATION_ADDRESS, foundation))
    remain = node.eth.getBalance(node.web3.toChecksumAddress(EconomicConfig.REMAIN_ACCOUNT_ADDRESS))
    log.info('Remaining total account address：{} amount：{}'.format(EconomicConfig.REMAIN_ACCOUNT_ADDRESS, remain))
    develop = node.eth.getBalance(node.web3.toChecksumAddress(EconomicConfig.DEVELOPER_FOUNDATAION_ADDRESS))
    log.info('Community developer foundation address：{} amount：{}'.format(EconomicConfig.DEVELOPER_FOUNDATAION_ADDRESS,
                                                                          develop))
    reality_total = foundation_louckup + incentive_pool + staking + foundation + remain + develop
    log.info("Total issuance of Chuangshi block：{}".format(reality_total))
    log.info("--------------Dividing line---------------")
    assert foundation == 0, "ErrMsg:Initial amount of foundation {}".format(foundation)
    assert foundation_louckup == 259096239000000000000000000, "ErrMsg:Initial lock up amount of foundation {}".format(
        foundation_louckup)
    assert staking == default_pledge_amount, "ErrMsg:Amount of initial pledge account: {}".format(staking)
    assert incentive_pool == 262215742000000000000000000, "ErrMsg:Initial amount of incentive pool {}".format(
        incentive_pool)
    assert remain == int(surplus_amount), "ErrMsg:Initial amount of remaining total account {}".format(remain)
    assert develop == 0, "ErrMsg:Community developer foundation account amount {}".format(develop)
    assert reality_total == EconomicConfig.TOKEN_TOTAL, "ErrMsg:Initialize release value {}".format(reality_total)


@allure.title("Two distribution-Transfer amount：{value}")
@pytest.mark.P0
@pytest.mark.parametrize('value', [1000, 0.000000000000000001, 100000000])
def test_IT_SD_004_to_006(client_consensus, value):
    """
    IT_SD_006:二次分配：普通钱包转keyshard钱包
    IT_SD_004:二次分配：转账金额为1von
    IT_SD_005:二次分配：转账金额为1亿LAT
    :param client_consensus:
    :param value:
    :return:
    """
    value = client_consensus.node.web3.toWei(value, 'ether')
    address, _ = client_consensus.economic.account.generate_account(client_consensus.node.web3, value)
    balance = client_consensus.node.eth.getBalance(address)
    log.info("transaction address：{},account：{}".format(address, balance))
    assert balance == value, "ErrMsg:Transfer amount {}".format(balance)


@pytest.mark.P1
@pytest.mark.parametrize('value', [2000, 1000])
def test_IT_SD_002_003(global_test_env, value):
    """
    IT_SD_002:二次分配：账户余额不足
    IT_SD_003:二次分配：转账手续费不足
    :param global_test_env:
    :param value:
    :return:
    """
    node = global_test_env.get_rand_node()
    address, _ = global_test_env.account.generate_account(node.web3, node.web3.toWei(1000, 'ether'))
    status = True
    # Account balance insufficient transfer
    try:
        address1, _ = global_test_env.account.generate_account(node.web3, 0)
        transfer_amount = node.web3.toWei(value, 'ether')
        result = global_test_env.account.sendTransaction(node.web3, '', address, address1, node.web3.platon.gasPrice,
                                                         21000, transfer_amount)
        log.info("result: {}".format(result))
        status = False
    except Exception as e:
        log.info("Use case success, exception information：{} ".format(str(e)))
    assert status, "ErrMsg:Transfer result {}".format(status)


@pytest.mark.P1
def test_IT_SD_011(global_test_env):
    """
    账户转账校验：转账gas费不足
    :param global_test_env:
    :return:
    """
    node = global_test_env.get_rand_node()
    address, _ = global_test_env.account.generate_account(node.web3, node.web3.toWei(1000, 'ether'))
    status = True
    # Insufficient gas fee for transfer
    try:
        address1, _ = global_test_env.account.generate_account(node.web3, 0)
        global_test_env.account.sendTransaction(node.web3, '', address,
                                                address1,
                                                node.web3.platon.gasPrice, 2100, 500)
        status = False
    except Exception as e:
        log.info("Use case success, exception information：{} ".format(str(e)))
    assert status, "ErrMsg:Transfer result {}".format(status)


@pytest.mark.P2
def test_IT_SD_007(global_test_env):
    """
    账户转账校验：本账户转本账户
    :return:
    """
    node = global_test_env.get_rand_node()
    value = node.web3.toWei(1000, 'ether')
    address, _ = global_test_env.account.generate_account(node.web3, value)
    balance = node.eth.getBalance(address)
    log.info("Account balance before transfer：{}".format(balance))
    result = global_test_env.account.sendTransaction(node.web3, '', address, address, node.eth.gasPrice, 21000, 100)
    assert result is not None, "ErrMsg:Transfer result {}".format(result)
    balance1 = node.eth.getBalance(address)
    log.info("Account balance after transfer： {}".format(balance1))
    log.info("Transaction fee： {}".format(node.web3.platon.gasPrice * 21000))
    assert balance == balance1 + node.web3.platon.gasPrice * 21000, "ErrMsg:Account balance after transfer：{}".format(
        balance1)


@pytest.mark.P0
def test_IT_SD_008(global_test_env):
    """
    二次分配：普通账户转platON基金会账户
    :return:
    """
    node = global_test_env.get_rand_node()
    value = node.web3.toWei(1000, 'ether')
    address, _ = global_test_env.account.generate_account(node.web3, value)
    balance = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    result = global_test_env.account.sendTransaction(node.web3, '', address, EconomicConfig.INCENTIVEPOOL_ADDRESS,
                                                     node.eth.gasPrice, 21000, node.web3.toWei(100, 'ether'))
    assert result is not None, "ErrMsg:Transfer result {}".format(result)
    balance1 = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("Account balance after transfer： {}".format(balance1))
    log.info("Transaction fee： {}".format(node.web3.platon.gasPrice * 21000))
    assert balance1 == balance + node.web3.toWei(100,
                                                 'ether') + node.web3.platon.gasPrice * 21000, "ErrMsg:Account balance after transfer：{}".format(
        balance1)


def sendTransaction_input_nonce(client, data, from_address, to_address, gasPrice, gas, value, nonce,
                                check_address=True):
    node = client.node
    account = client.economic.account.accounts[from_address]
    print(account)
    if check_address:
        to_address = Web3.toChecksumAddress(to_address)
    tmp_from_address = Web3.toChecksumAddress(from_address)

    transaction_dict = {
        "to": to_address,
        "gasPrice": gasPrice,
        "gas": gas,
        "nonce": nonce,
        "data": data,
        "chainId": client.economic.account.chain_id,
        "value": value,
        'from': tmp_from_address,
    }
    signedTransactionDict = node.eth.account.signTransaction(
        transaction_dict, account['prikey']
    )
    data = signedTransactionDict.rawTransaction
    result = HexBytes(node.eth.sendRawTransaction(data)).hex()
    res = node.eth.waitForTransactionReceipt(result)

    return res


@pytest.mark.P2
def test_IT_SD_009(client_consensus):
    """
    同一时间多次转账
    :return:
    """
    client = client_consensus
    economic = client.economic
    node = client.node
    economic.env.deploy_all()
    address, _ = economic.account.generate_account(node.web3, node.web3.toWei(1000, 'ether'))
    address1, _ = economic.account.generate_account(node.web3, 0)
    nonce = node.eth.getTransactionCount(address)
    print('nonce: ', nonce)
    balance = node.eth.getBalance(address1)
    log.info("balance: {}".format(balance))
    sendTransaction_input_nonce(client, '', address, address1, node.eth.gasPrice, 21000, node.web3.toWei(100, 'ether'),
                                nonce)
    sendTransaction_input_nonce(client, '', address, address1, node.eth.gasPrice, 21000, node.web3.toWei(100, 'ether'),
                                nonce + 1)
    time.sleep(3)
    balance1 = node.eth.getBalance(address1)
    log.info("Account balance after transfer： {}".format(balance1))
    assert balance1 == balance + node.web3.toWei(200, 'ether'), "ErrMsg:Account balance after transfer：{}".format(
        balance1)


@pytest.mark.P2
def test_IT_SD_010(client_consensus):
    """
    同一时间多次转账，余额不足
    :return:
    """
    client = client_consensus
    economic = client.economic
    node = client.node
    economic.env.deploy_all()
    address, _ = economic.account.generate_account(node.web3, node.web3.toWei(1000, 'ether'))
    address1, _ = economic.account.generate_account(node.web3, 0)
    balance = node.eth.getBalance(address1)
    log.info("balance: {}".format(balance))
    try:
        nonce = node.eth.getTransactionCount(address)
        log.info('nonce: {}'.format(nonce))
        sendTransaction_input_nonce(client, '', address, address1, node.eth.gasPrice, 21000,
                                    node.web3.toWei(500, 'ether'), nonce)
        sendTransaction_input_nonce(client, '', address, address1, node.eth.gasPrice, 21000,
                                    node.web3.toWei(600, 'ether'), nonce + 1)
    except Exception as e:
        log.info("Use case success, exception information：{} ".format(str(e)))
        time.sleep(3)
        balance1 = node.eth.getBalance(address1)
        log.info("Account balance after transfer： {}".format(balance1))
        assert balance1 == balance + node.web3.toWei(500, 'ether'), "ErrMsg:Account balance after transfer：{}".format(
            balance1)


def consensus_node_pledge_award_assertion(client, address):
    """
    内置节点质押奖励断言
    :param client:
    :param address:
    :return:
    """
    blockNumber = client.node.eth.blockNumber
    log.info("Current block height：{}".format(blockNumber))
    incentive_pool_balance = client.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("Balance of incentive pool：{}".format(incentive_pool_balance))
    CandidateInfo = client.ppos.getCandidateInfo(client.node.node_id)
    log.info("Pledgor node information：{}".format(CandidateInfo))

    # wait settlement block
    client.economic.wait_settlement_blocknum(client.node)
    time.sleep(5)
    VerifierList = client.ppos.getVerifierList()
    log.info("Current settlement cycle verifier list：{}".format(VerifierList))
    ValidatorList = client.ppos.getValidatorList()
    log.info("Current consensus cycle verifier list：{}".format(ValidatorList))
    # Application for withdrew staking
    result = client.staking.withdrew_staking(address)
    assert_code(result, 0)
    # wait settlement block
    client.economic.wait_settlement_blocknum(client.node)
    # view incentive pool amonut
    incentive_pool_balance2 = client.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info(
        "incentive pool address：{} amount：{}".format(EconomicConfig.INCENTIVEPOOL_ADDRESS, incentive_pool_balance2))
    assert incentive_pool_balance2 - incentive_pool_balance < client.node.web3.toWei(1,
                                                                                                  'ether'), "ErrMsg:Balance of incentive pool：{}".format(
        incentive_pool_balance2)


def no_consensus_node_pledge_award_assertion(client, benifit_address, from_address):
    """
    非内置节点质押奖励断言
    :param client:
    :param benifit_address:
    :param from_address:
    :return:
    """
    CandidateInfo = client.ppos.getCandidateInfo(client.node.node_id)
    log.info("Pledgor node information：{}".format(CandidateInfo))
    balance = client.node.eth.getBalance(benifit_address)
    log.info("benifit address：{} amount： {}".format(benifit_address, balance))

    # wait settlement block
    client.economic.wait_settlement_blocknum(client.node)
    time.sleep(5)
    VerifierList = client.ppos.getVerifierList()
    log.info("Current settlement cycle verifier list：{}".format(VerifierList))
    ValidatorList = client.ppos.getValidatorList()
    log.info("Current consensus cycle verifier list：{}".format(ValidatorList))
    block_reward, staking_reward = client.economic.get_current_year_reward(client.node)
    for i in range(4):
        result = check_node_in_list(client.node.node_id, client.ppos.getValidatorList)
        log.info("Current node in consensus list status：{}".format(result))
        if result:
            # wait consensus block
            client.economic.wait_consensus_blocknum(client.node)
            # Application for withdrew staking
            result = client.staking.withdrew_staking(from_address)
            assert_code(result, 0)
            incentive_pool_balance1 = client.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
            log.info("incentive pool amount：{}".format(incentive_pool_balance1))
            # wait settlement block
            client.economic.wait_settlement_blocknum(client.node)
            # Count the number of blocks out of pledge node
            blocknumber = client.economic.get_block_count_number(client.node, 5)
            log.info("blocknumber: {}".format(blocknumber))
            balance1 = client.node.eth.getBalance(benifit_address)
            log.info("benifit address：{} amount：{}".format(benifit_address, balance1))

            # Verify block rewards
            log.info("Expected bonus：{}".format(Decimal(str(block_reward)) * blocknumber))
            assert balance + Decimal(str(block_reward)) * blocknumber - balance1 < client.node.web3.toWei(
                1, 'ether'), "ErrMsg:benifit address：{} amount：{}".format(
                benifit_address, balance1)
            break
        else:
            # wait consensus block
            client.economic.wait_consensus_blocknum(client.node)


@pytest.mark.p1
def test_AL_IE_001(client_consensus):
    """
    查看初始激励池总额
    :param client_consensus:
    :return:
    """
    client = client_consensus
    economic = client.economic
    node = client.node
    # 初始化环境
    client.economic.env.deploy_all()
    # 查询激励池初始金额
    incentive_pool = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS, 0)
    assert incentive_pool == 262215742000000000000000000, "ErrMsg:Initial amount of incentive pool {}".format(
        incentive_pool)


@pytest.mark.P2
def test_AL_IE_002(clients_new_node):
    """
    转账到激励池
    :param clients_new_node:
    :return:
    """
    client1 = clients_new_node[0]
    client2 = clients_new_node[1]
    economic = client1.economic
    node = client1.node
    log.info("Node ID：{}".format(node.node_id))
    log.info("Current connection node： {}".format(node.node_mark))
    address, _ = client1.economic.account.generate_account(node.web3, von_amount(economic.create_staking_limit, 4))
    address1, _ = client1.economic.account.generate_account(node.web3, 0)
    address2, _ = client1.economic.account.generate_account(node.web3, 0)
    log.info("staking address: {}".format(address))
    # Free amount application pledge node
    result = client1.staking.create_staking(0, address1, address)
    assert_code(result, 0)
    # Wait for the settlement round to end
    economic.wait_settlement_blocknum(node)
    # 获取当前结算周期验证人
    verifier_list = node.ppos.getVerifierList()
    log.info("verifier_list: {}".format(verifier_list))
    # view block_reward
    block_reward, staking_reward = economic.get_current_year_reward(node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    # view account amount
    benifit_balance = node.eth.getBalance(address1)
    log.info("benifit_balance: {}".format(benifit_balance))
    # view benifit reward
    blocknumber = view_benifit_reward(client1, address)
    # view account amount again
    benifit_balance1 = node.eth.getBalance(address1)
    log.info("benifit_balance: {}".format(benifit_balance1))
    reward = int(blocknumber * Decimal(str(block_reward)))
    assert benifit_balance1 == staking_reward + reward, "ErrMsg:benifit_balance: {}".format(benifit_balance1)
    # Transfer to the incentive pool
    result = client1.economic.account.sendTransaction(node.web3, '', address, EconomicConfig.INCENTIVEPOOL_ADDRESS,
                                                      node.eth.gasPrice, 21000, node.web3.toWei(1000, 'ether'))
    assert result is not None, "ErrMsg:Transfer result {}".format(result)
    time.sleep(5)
    # Free amount application pledge node
    result = client2.staking.create_staking(0, address2, address, amount=von_amount(economic.create_staking_limit, 2))
    assert_code(result, 0)
    # Wait for the settlement round to end
    economic.wait_settlement_blocknum(client2.node)
    # view account amount
    benifit_balance2 = client2.node.eth.getBalance(address2)
    log.info("benifit_balance: {}".format(benifit_balance2))
    # view benifit reward
    blocknumber = view_benifit_reward(client2, address)
    # view account amount again
    benifit_balance3 = client2.node.eth.getBalance(address2)
    log.info("benifit_balance: {}".format(benifit_balance3))
    reward = int(blocknumber * Decimal(str(block_reward)))
    assert benifit_balance3 == staking_reward + reward, "ErrMsg:benifit_balance: {}".format(benifit_balance3)


@pytest.mark.P1
def test_AL_IE_003(clients_new_node):
    """
    自由账户创建质押节点且收益地址为激励池
    :param clients_new_node:
    :return:
    """
    log.info("Node ID：{}".format(clients_new_node[0].node.node_id))
    address, _ = clients_new_node[0].economic.account.generate_account(clients_new_node[0].node.web3,
                                                                       clients_new_node[
                                                                           0].economic.create_staking_limit * 2)
    log.info("staking address: {}".format(address))
    # Free amount application pledge node
    result = clients_new_node[0].staking.create_staking(0, EconomicConfig.INCENTIVEPOOL_ADDRESS, address)
    assert_code(result, 0)
    consensus_node_pledge_award_assertion(clients_new_node[0], address)


@pytest.mark.P1
def test_AL_IE_004(clients_new_node):
    """
    锁仓账户创建质押节点且收益地址为激励池
    :param clients_new_node:
    :return:
    """
    log.info("Node ID：{}".format(clients_new_node[1].node.node_id))
    address, _ = clients_new_node[1].economic.account.generate_account(clients_new_node[1].node.web3,
                                                                       clients_new_node[
                                                                           1].economic.create_staking_limit * 2)
    log.info("staking address: {}".format(address))
    # Create restricting plan
    staking_amount = clients_new_node[1].economic.create_staking_limit
    log.info("staking amonut：{}".format(staking_amount))
    plan = [{'Epoch': 1, 'Amount': staking_amount}]
    result = clients_new_node[1].restricting.createRestrictingPlan(address, plan, address)
    assert_code(result, 0)
    # Lock in amount application pledge node
    result = clients_new_node[1].staking.create_staking(1, EconomicConfig.INCENTIVEPOOL_ADDRESS, address)
    assert_code(result, 0)
    consensus_node_pledge_award_assertion(clients_new_node[1], address)


@pytest.mark.P1
def test_AL_BI_001(client_consensus):
    """
    出块手续费奖励（内置验证人）
    :param client_consensus:
    :return:
    """
    incentive_pool_balance = client_consensus.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("incentive_pool_balance: {}".format(incentive_pool_balance))
    # create account
    address1, _ = client_consensus.economic.account.generate_account(client_consensus.node.web3, 100)
    # view incentive account
    incentive_pool_balance1 = client_consensus.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("incentive_pool_balance: {}".format(incentive_pool_balance1))
    assert incentive_pool_balance1 == incentive_pool_balance + 21000 * client_consensus.node.eth.gasPrice, "ErrMsg:incentive_pool balance: {}".format(
        incentive_pool_balance1)


@pytest.mark.P1
def test_AL_BI_002(new_genesis_env, staking_cfg):
    """
    节点出块率为0被处罚，激励池金额增加
    :param new_genesis_env:
    :param staking_cfg:
    :return:
    """
    # Change configuration parameters
    genesis = from_dict(data_class=Genesis, data=new_genesis_env.genesis_config)
    genesis.economicModel.slashing.slashBlocksReward = 5
    new_file = new_genesis_env.cfg.env_tmp + "/genesis.json"
    genesis.to_file(new_file)
    new_genesis_env.deploy_all(new_file)
    client_noc_list_obj = get_clients_noconsensus(new_genesis_env, staking_cfg)
    client1 = client_noc_list_obj[0]
    client2 = client_noc_list_obj[1]
    economic = client1.economic
    node = client1.node
    log.info("nodeid: {}".format(node.node_id))
    # create account
    address, _ = economic.account.generate_account(node.web3, von_amount(economic.create_staking_limit, 2))
    # create staking
    result = client1.staking.create_staking(0, address, address)
    assert_code(result, 0)
    # Waiting for a settlement round
    client2.economic.wait_settlement_blocknum(client2.node)
    # view incentive account
    incentive_pool_balance = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("incentive_pool_balance: {}".format(incentive_pool_balance))
    # view block_reward
    block_reward, staking_reward = economic.get_current_year_reward(node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    # stop node
    node.stop()
    # Waiting for 2 consensus round
    client2.economic.wait_consensus_blocknum(client2.node, 2)
    # view verifier list
    verifier_list = client2.ppos.getVerifierList()
    log.info("verifier_list: {}".format(verifier_list))
    slash_blocks = get_governable_parameter_value(client2, 'slashBlocksReward')
    # Get the penalty amount
    penalty_amount = int(Decimal(str(block_reward)) * Decimal(str(slash_blocks)))
    log.info("penalty_amount: {}".format(penalty_amount))
    # view incentive account again
    incentive_pool_balance1 = client2.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("incentive_pool_balance1: {}".format(incentive_pool_balance1))
    assert incentive_pool_balance1 == incentive_pool_balance + penalty_amount, "ErrMsg: incentive_pool_balance: {}".format(
        incentive_pool_balance1)


@pytest.mark.P1
def test_AL_BI_003(client_consensus):
    """
    初始内置账户没有基金会Staking奖励和出块奖励
    :param client_consensus:
    :return:
    """
    # view incentive account
    incentive_pool_balance = client_consensus.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("incentive_pool_balance: {}".format(incentive_pool_balance))

    # wait settlement block
    client_consensus.economic.wait_settlement_blocknum(client_consensus.node)

    # view incentive account again
    incentive_pool_balance1 = client_consensus.node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS)
    log.info("incentive_pool_balance: {}".format(incentive_pool_balance1))

    assert incentive_pool_balance1 == incentive_pool_balance, "ErrMsg: incentive account: {}".format(
        incentive_pool_balance1)


@pytest.mark.P1
def test_AL_BI_004(client_consensus):
    """
    初始验证人退出后重新质押进来
    :param client_consensus:
    :return:
    """
    client = client_consensus
    economic = client.economic
    node = client.node
    # Reset environment
    economic.env.deploy_all()
    # Query Developer Fund Amount
    log.info("nodeid: {}".format(node.node_id))
    developer_foundation_balance = node.eth.getBalance(EconomicConfig.DEVELOPER_FOUNDATAION_ADDRESS)
    log.info("incentive_pool_balance: {}".format(developer_foundation_balance))
    staking_balance = client_consensus.node.eth.getBalance(EconomicConfig.STAKING_ADDRESS)
    log.info("staking_balance: {}".format(staking_balance))
    # Built in node return pledge
    result = client.staking.withdrew_staking(EconomicConfig.DEVELOPER_FOUNDATAION_ADDRESS)
    assert_code(result, 0)
    # Waiting for the end of the 2 settlement
    economic.wait_settlement_blocknum(node, 2)
    # create account
    address, _ = economic.account.generate_account(node.web3, von_amount(economic.create_staking_limit, 2))
    address1, _ = economic.account.generate_account(node.web3, 0)
    # Check account balance
    balance = node.eth.getBalance(address1)
    log.info("Account Balance： {}".format(balance))
    # Node pledge again
    result = client.staking.create_staking(0, address1, address)
    assert_code(result, 0)
    # Waiting for the end of the settlement
    economic.wait_settlement_blocknum(node)
    # view block_reward
    block_reward, staking_reward = economic.get_current_year_reward(node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    # withdrew of pledge
    result = client.staking.withdrew_staking(address)
    assert_code(result, 0)
    # wait settlement block
    client.economic.wait_settlement_blocknum(client.node)
    # wait consensus block
    client.economic.wait_consensus_blocknum(client.node)
    # count the number of blocks
    blocknumber = client.economic.get_block_count_number(client.node, 10)
    log.info("blocknumber: {}".format(blocknumber))
    # Check account balance again
    balance1 = node.eth.getBalance(address1)
    log.info("Account Balance： {}".format(balance1))
    # Pledged income account to get the bonus amount
    total_reward = int(Decimal(str(block_reward)) * blocknumber) + staking_reward
    assert balance1 == balance + total_reward, "ErrMsg:benifit_balance: {}".format(balance1)


def create_pledge_node(client, base, multiple=2):
    """
    create pledge node return benifit balance
    :param client:
    :param base:
    :param multiple:
    :return:
    """
    log.info("Transfer accounts: {}".format(client.economic.create_staking_limit * multiple))
    account_balance = client.node.eth.getBalance(
        client.economic.account.account_with_money['address'])
    log.info("address: {} accounts: {}".format(client.economic.account.account_with_money['address'],
                                               account_balance))
    # create account
    address, _ = client.economic.account.generate_account(client.node.web3,
                                                                       client.economic.create_staking_limit * multiple)

    log.info("address: {} ,amount: {}".format(address, client.node.eth.getBalance(address)))
    benifit_address, _ = client.economic.account.generate_account(client.node.web3, 0)
    log.info(
        "address: {} ,amount: {}".format(benifit_address, client.node.eth.getBalance(benifit_address)))
    # create staking
    staking_amount = von_amount(client.economic.create_staking_limit, base)
    result = client.staking.create_staking(0, benifit_address, address, amount=staking_amount)
    assert_code(result, 0)
    log.info("Pledge node information: {}".format(
        client.ppos.getCandidateInfo(client.node.node_id)))
    return address, benifit_address


@pytest.mark.P1
def test_AL_NBI_001_to_003(client_new_node):
    """
    AL_NBI_001:非内置验证人Staking奖励（犹豫期）
    AL_NBI_002:非内置验证人出块奖励（犹豫期）
    AL_NBI_003:非内置验证人区块手续费奖励（犹豫期）
    :param client_new_node:
    :return:
    """
    # create pledge node
    address, benifit_address = create_pledge_node(client_new_node, 1)
    # view account amount
    benifit_balance = client_new_node.node.eth.getBalance(benifit_address)
    log.info("benifit_balance: {}".format(benifit_balance))
    # wait consensus block
    client_new_node.economic.wait_consensus_blocknum(client_new_node.node)
    # view account amount again
    benifit_balance1 = client_new_node.node.eth.getBalance(benifit_address)
    log.info("benifit_balance: {}".format(benifit_balance1))
    assert benifit_balance1 == benifit_balance, "ErrMsg:benifit_balance: {}".format(
        benifit_balance1)


@pytest.mark.P1
def test_AL_NBI_004_to_006(new_genesis_env, client_new_node, reset_environment):
    """
    AL_NBI_004:非内置验证人Staking奖励（候选人）
    AL_NBI_005:非内置验证人出块奖励（候选人）
    AL_NBI_006:非内置验证人手续费奖励（候选人）
    :param new_genesis_env:
    :return:
    """
    # Change configuration parameters
    genesis = from_dict(data_class=Genesis, data=new_genesis_env.genesis_config)
    genesis.economicModel.staking.maxValidators = 4
    new_file = new_genesis_env.cfg.env_tmp + "/genesis.json"
    genesis.to_file(new_file)
    new_genesis_env.deploy_all(new_file)
    # create pledge node
    address, benifit_address = create_pledge_node(client_new_node, 1)
    # view account amount
    benifit_balance = client_new_node.node.eth.getBalance(benifit_address)
    log.info("benifit_balance: {}".format(benifit_balance))
    # wait settlement block
    client_new_node.economic.wait_settlement_blocknum(client_new_node.node, 1)
    # view account amount again
    benifit_balance1 = client_new_node.node.eth.getBalance(benifit_address)
    log.info("benifit_balance: {}".format(benifit_balance1))
    assert benifit_balance1 == benifit_balance, "ErrMsg: benifit_balance: {}".format(
        benifit_balance1)


def view_benifit_reward(client, address):
    """
    withdrew pledge return benifit balance and Number of blocks
    :param client:
    :param address:
    :return:
    """
    # withdrew of pledge
    result = client.staking.withdrew_staking(address)
    assert_code(result, 0)
    # wait settlement block
    client.economic.wait_settlement_blocknum(client.node)
    # wait consensus block
    client.economic.wait_consensus_blocknum(client.node)
    # count the number of blocks
    blocknumber = client.economic.get_block_count_number(client.node, 10)
    log.info("blocknumber: {}".format(blocknumber))
    return blocknumber


@pytest.mark.P1
@pytest.mark.compatibility
def test_AL_NBI_007_to_009(client_new_node):
    """
    AL_NBI_007:非内置验证人Staking奖励（验证人）
    AL_NBI_008:非内置验证人出块奖励（验证人）
    AL_NBI_009:非内置验证人手续费奖励（验证人）
    :param client_new_node:
    :return:
    """
    # create pledge node
    address, benifit_address = create_pledge_node(client_new_node, 1.1)
    # view account amount
    benifit_balance = client_new_node.node.eth.getBalance(benifit_address)
    log.info("benifit_balance: {}".format(benifit_balance))
    # wait settlement block
    client_new_node.economic.wait_settlement_blocknum(client_new_node.node)
    # view block_reward
    block_reward, staking_reward = client_new_node.economic.get_current_year_reward(
        client_new_node.node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    for i in range(4):
        result = check_node_in_list(client_new_node.node.node_id, client_new_node.ppos.getValidatorList)
        log.info("Current node in consensus list status：{}".format(result))
        if not result:
            # view benifit reward
            blocknumber = view_benifit_reward(client_new_node, address)
            # view account amount again
            benifit_balance1 = client_new_node.node.eth.getBalance(benifit_address)
            log.info("benifit_balance: {}".format(benifit_balance1))
            assert benifit_balance + staking_reward + blocknumber * Decimal(
                str(block_reward)) - benifit_balance1 < client_new_node.node.web3.toWei(1,
                                                                                        'ether'), "ErrMsg:benifit_balance: {}".format(
                benifit_balance1)
            break
        else:
            # wait consensus block
            client_new_node.economic.wait_consensus_blocknum(client_new_node.node)


def assert_benifit_reward(client, benifit_address, address):
    """
    assert Amount of income address
    :param client:
    :param benifit_address:
    :param address:
    :return:
    """
    # view account amount
    benifit_balance = client.node.eth.getBalance(benifit_address)
    log.info("benifit_balance: {}".format(benifit_balance))
    # wait settlement block
    client.economic.wait_settlement_blocknum(client.node)
    # view block_reward
    block_reward, staking_reward = client.economic.get_current_year_reward(
        client.node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    for i in range(4):
        result = check_node_in_list(client.node.node_id, client.ppos.getValidatorList)
        log.info("Current node in consensus list status：{}".format(result))
        if result:
            # view benifit reward
            blocknumber = view_benifit_reward(client, address)
            # view account amount again
            benifit_balance1 = client.node.eth.getBalance(benifit_address)
            log.info("benifit_balance: {}".format(benifit_balance1))
            assert benifit_balance + staking_reward + blocknumber * Decimal(
                str(block_reward)) - benifit_balance1 < client.node.web3.toWei(1,
                                                                                            'ether'), "ErrMsg:benifit_balance: {}".format(
                benifit_balance1)
            break
        else:
            # wait consensus block
            client.economic.wait_consensus_blocknum(client.node)


@pytest.mark.P1
def test_AL_NBI_010_to_012(client_new_node):
    """
    AL_NBI_010:非内置验证人Staking奖励（共识验证人）
    AL_NBI_011:非内置验证人出块奖励（共识验证人）
    AL_NBI_012:非内置验证人手续费出块奖励（共识验证人）
    :param client_new_node:
    :return:
    """
    # create pledge node
    address, benifit_address = create_pledge_node(client_new_node, 1.2)
    # assert benifit reward
    assert_benifit_reward(client_new_node, benifit_address, address)


@pytest.mark.P1
def test_AL_NBI_013(client_new_node):
    """
    修改节点质押收益地址查看收益变更
    :param client_new_node:
    :return:
    """
    # create pledge node
    address, benifit_address = create_pledge_node(client_new_node, 1.3)
    # create account
    benifit_address1, _ = client_new_node.economic.account.generate_account(client_new_node.node.web3, 0)
    # change benifit address
    result = client_new_node.staking.edit_candidate(address, benifit_address1)
    assert_code(result, 0)
    # assert benifit reward
    assert_benifit_reward(client_new_node, benifit_address1, address)


def query_ccount_amount(client, address):
    balance = client.node.eth.getBalance(address)
    log.info("balance: {}".format(balance))
    return balance


@pytest.mark.P1
def test_AL_NBI_014(client_new_node):
    """
    修改节点质押收益地址查看收益变更（正在出块中）
    :param client_new_node:
    :return:
    """
    # create pledge node
    address, benifit_address = create_pledge_node(client_new_node, 1.4)
    # wait settlement block
    client_new_node.economic.wait_settlement_blocknum(client_new_node.node)
    # view block_reward
    block_reward, staking_reward = client_new_node.economic.get_current_year_reward(
        client_new_node.node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    # view benifit_address amount again
    benifit_balance = query_ccount_amount(client_new_node, benifit_address)
    # change benifit address
    for i in range(4):
        result = check_node_in_list(client_new_node.node.node_id, client_new_node.ppos.getValidatorList)
        log.info("Current node in consensus list status：{}".format(result))
        if result:
            current_block = client_new_node.node.eth.blockNumber
            log.info("Current block:{}".format(current_block))
            for i in range(40):
                nodeid = get_pub_key(client_new_node.node.url, current_block)
                current_block = client_new_node.node.eth.blockNumber
                log.info("当前块高:{}".format(current_block))
                time.sleep(3)
                if nodeid == client_new_node.node.node_id:
                    break
            # create account
            benifit_address1, _ = client_new_node.economic.account.generate_account(client_new_node.node.web3, 0)
            # change benifit address
            result = client_new_node.staking.edit_candidate(address, benifit_address1)
            assert_code(result, 0)
            # view benifit reward
            blocknumber = view_benifit_reward(client_new_node, address)

            # view benifit_address1 amount
            benifit_balance1 = query_ccount_amount(client_new_node, benifit_address1)
            assert benifit_balance + benifit_balance1 == int(Decimal(str(
                block_reward)) * blocknumber) + staking_reward, "ErrMsg:benifit_balance + benifit_balance1: {}".format(
                benifit_balance + benifit_balance1)


@pytest.mark.P1
def test_AL_NBI_015(client_new_node):
    """
    退回质押金并处于锁定期
    :param client_new_node:
    :return:
    """
    # create pledge node
    address, benifit_address = create_pledge_node(client_new_node, 1.5)
    # wait settlement block
    client_new_node.economic.wait_settlement_blocknum(client_new_node.node)
    # view account amount
    benifit_balance = query_ccount_amount(client_new_node, benifit_address)
    for i in range(4):
        result = check_node_in_list(client_new_node.node.node_id, client_new_node.ppos.getValidatorList)
        log.info("Current node in consensus list status：{}".format(result))
        if result:
            # withdrew of pledge
            result = client_new_node.staking.withdrew_staking(address)
            assert_code(result, 0)
            log.info("Current settlement cycle verifier list：{}".format(client_new_node.ppos.getVerifierList()))
            for i in range(40):
                client_new_node.economic.account.sendTransaction(client_new_node.node.web3, '',
                                                                 client_new_node.economic.account.account_with_money[
                                                                     'address'], address,
                                                                 client_new_node.node.web3.platon.gasPrice,
                                                                 21000, 100)
                time.sleep(1)
            # view account amount again
            benifit_balance1 = query_ccount_amount(client_new_node, benifit_address)
            assert benifit_balance1 > benifit_balance, "ErrMsg: {} > {}".format(benifit_balance1, benifit_balance)
            break
        else:
            # wait consensus block
            client_new_node.economic.wait_consensus_blocknum(client_new_node.node)


@pytest.mark.P2
@pytest.mark.compatibility
def test_AL_NBI_016(client_new_node, reset_environment):
    """
    被双签处罚槛剔除验证人列表
    :param client_new_node:
    :return:
    """
    client = client_new_node
    economic = client.economic
    node = client.node
    client.economic.env.deploy_all()
    # create account
    address1, _ = economic.account.generate_account(node.web3, von_amount(economic.create_staking_limit, 2))
    address2, _ = economic.account.generate_account(node.web3, 0)
    report_address, _ = economic.account.generate_account(node.web3, node.web3.toWei(1000, 'ether'))
    # create staking
    staking_amount = von_amount(economic.create_staking_limit, 1.6)
    result = client_new_node.staking.create_staking(0, address2, address1, amount=staking_amount)
    assert_code(result, 0)
    # wait settlement block
    economic.wait_settlement_blocknum(node)
    # Check account balance
    balance = node.eth.getBalance(address2)
    log.info("Account Balance：{}".format(balance))
    # view block_reward
    block_reward, staking_reward = economic.get_current_year_reward(node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    for i in range(4):
        result = check_node_in_list(client_new_node.node.node_id, client_new_node.ppos.getValidatorList)
        log.info("Current node in consensus list status：{}".format(result))
        if result:
            # view Current block
            current_block = client_new_node.node.eth.blockNumber
            log.info("Current block: {}".format(current_block))
            # Report prepareblock signature
            report_information = mock_duplicate_sign(1, client_new_node.node.nodekey, client_new_node.node.blsprikey,
                                                     current_block)
            log.info("Report information: {}".format(report_information))
            result = client_new_node.duplicatesign.reportDuplicateSign(1, report_information, report_address)
            assert_code(result, 0)
            # wait settlement block
            economic.wait_settlement_blocknum(node)
            # Check account balance again
            balance1 = node.eth.getBalance(address2)
            log.info("Account Balance：{}".format(balance1))
            # count the number of blocks
            blocknumber = client_new_node.economic.get_block_count_number(node, 10)
            log.info("blocknumber: {}".format(blocknumber))
            total_block_reward = int(Decimal(str(block_reward)) * Decimal(str(blocknumber)))
            log.info("total_block_reward: {}".format(total_block_reward))
            assert balance1 == balance + total_block_reward, "ErrMsg:benifit_balance1：{}".format(balance1)
            break
        else:
            # wait consensus block
            economic.wait_consensus_blocknum(node)


@pytest.mark.P2
@pytest.mark.compatibility
def test_AL_NBI_017(clients_new_node):
    """
    0出块率剔除验证人列表
    :param clients_new_node:
    :return:
    """
    clients_new_node[0].economic.env.deploy_all()
    # create pledge node
    address, benifit_address = create_pledge_node(clients_new_node[0], 1.6)
    # wait settlement block
    clients_new_node[0].economic.wait_settlement_blocknum(clients_new_node[0].node)
    log.info("Current settlement cycle verifier list：{}".format(clients_new_node[0].ppos.getVerifierList()))
    # view block_reward
    block_reward, staking_reward = clients_new_node[0].economic.get_current_year_reward(
        clients_new_node[0].node)
    log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    # view account amount
    benifit_balance = query_ccount_amount(clients_new_node[0], benifit_address)
    for i in range(4):
        result = check_node_in_list(clients_new_node[0].node.node_id, clients_new_node[0].ppos.getValidatorList)
        log.info("Current node in consensus list status：{}".format(result))
        if result:
            # stop node
            clients_new_node[0].node.stop()
            log.info("Current settlement cycle verifier list：{}".format(clients_new_node[1].ppos.getVerifierList()))
            # wait settlement block
            clients_new_node[1].economic.wait_settlement_blocknum(clients_new_node[1].node)
            # view account amount again
            benifit_balance1 = query_ccount_amount(clients_new_node[1], benifit_address)
            # count the number of blocks
            blocknumber = clients_new_node[1].economic.get_block_count_number(clients_new_node[1].node, 5)
            log.info("blocknumber: {}".format(blocknumber))
            assert benifit_balance1 == benifit_balance + int(
                Decimal(str(block_reward)) * blocknumber), "ErrMsg:benifit_balance1：{}".format(benifit_balance1)
            break
        else:
            # wait consensus block
            clients_new_node[0].economic.wait_consensus_blocknum(clients_new_node[0].node)


@pytest.mark.P1
def test_AL_NBI_018(new_genesis_env, client_new_node):
    """
    调整质押和出块奖励比例
    :param client_new_node:
    :return:
    """
    # Change configuration parameters
    genesis = from_dict(data_class=Genesis, data=new_genesis_env.genesis_config)
    genesis.economicModel.reward.newBlockRate = 60
    new_file = new_genesis_env.cfg.env_tmp + "/genesis.json"
    genesis.to_file(new_file)
    new_genesis_env.deploy_all(new_file)

    client = client_new_node
    economic = client.economic
    node = client.node
    # create account
    address1, _ = economic.account.generate_account(node.web3, von_amount(economic.create_staking_limit, 2))
    address2, _ = economic.account.generate_account(node.web3, 0)
    # create pledge
    result = client.staking.create_staking(0, address1, address1)
    assert_code(result, 0)
    # Waiting for the end of the settlement
    economic.wait_settlement_blocknum(node)
    # Check account balance
    balance = node.eth.getBalance(address1)
    log.info("Account Balance： {}".format(balance))
    block_reward, staking_reward = economic.get_current_year_reward(node, new_block_rate=60)
    # # Get the number of certifiers in the billing cycle list
    # verifier_list = get_pledge_list(node.ppos.getVerifierList)
    # verifier_num = len(verifier_list)
    # # Get block_reward And pledge rewards
    # amount = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS, 0)
    # block_proportion = str(60 / 100)
    # staking_proportion = str(1 - 60 / 100)
    # block_reward = int(Decimal(str(amount)) * Decimal(str(block_proportion)) / Decimal(str(1600)))
    # staking_reward = int(Decimal(str(amount)) * Decimal(str(staking_proportion)) / Decimal(str(10)) / Decimal(
    #     str(verifier_num)))
    # log.info("block_reward: {} staking_reward: {}".format(block_reward, staking_reward))
    # withdrew of pledge
    result = client.staking.withdrew_staking(address1)
    assert_code(result, 0)
    # wait settlement block
    client.economic.wait_settlement_blocknum(client.node)
    # wait consensus block
    client.economic.wait_consensus_blocknum(client.node)
    # count the number of blocks
    blocknumber = client.economic.get_block_count_number(client.node, 10)
    log.info("blocknumber: {}".format(blocknumber))
    # Check account balance again
    balance1 = node.eth.getBalance(address1)
    log.info("Account Balance： {}".format(balance1))
    # Pledged income account to get the bonus amount
    total_reward = int(Decimal(str(block_reward)) * blocknumber) + staking_reward
    log.info("total:{}".format(balance + total_reward))
    assert balance + total_reward - balance1 < node.web3.toWei(1, 'ether'), "ErrMsg:benifit_balance: {}".format(
        balance1)

    # # create pledge node
    # address, benifit_address = create_pledge_node(client_new_node, 1.2)
    # # assert benifit reward
    # assert_benifit_reward(client_new_node, benifit_address, address)


def calculate_block_rewards_and_pledge_rewards(client, incentive_pool_amount, annual_size, annualcycle):
    new_block_rate = client.economic.genesis.economicModel.reward.newBlockRate
    block_proportion = str(new_block_rate / 100)
    log.info("Get incentive pool to allocate block reward ratio: {}".format(block_proportion))
    verifier_list = get_pledge_list(client.node.ppos.getVerifierList)
    verifier_num = len(verifier_list)
    log.info("Number of verification nodes in the current settlement cycle： {}".format(verifier_num))
    total_block_rewards = int(Decimal(str(incentive_pool_amount)) * Decimal(str(block_proportion)))
    per_block_reward = total_block_rewards / Decimal(str(annual_size))
    log.info("Total block rewards: {} Each block reward: {}".format(total_block_rewards, per_block_reward))
    staking_reward_total = incentive_pool_amount - total_block_rewards
    staking_reward = int(Decimal(str(staking_reward_total)) / Decimal(str(annualcycle)) / Decimal(str(verifier_num)))
    log.info("Total pledged rewards: {} Amount of pledged rewards in current settlement cycle: {}".format(
        staking_reward_total, staking_reward))
    return per_block_reward, staking_reward


def test_AL_NBI_019(client_consensus):
    """
    验证第一个结算周期区块奖励和质押奖励
    :param client_consensus:
    :return:
    """
    client = client_consensus
    economic = client.economic
    node = client.node
    log.info("Start resetting the chain")
    economic.env.deploy_all()
    time.sleep(5)
    incentive_pool_balance = 262215742000000000000000000
    log.info("Get the initial value of the incentive pool：{}".format(incentive_pool_balance))
    annualcycle = (economic.additional_cycle_time * 60) // economic.settlement_size
    annual_size = annualcycle * economic.settlement_size
    log.info("Block height of current issuance cycle: {}".format(annual_size))
    per_block_reward, staking_reward = calculate_block_rewards_and_pledge_rewards(client, incentive_pool_balance, annual_size, annualcycle)
    result = client.ppos.getPackageReward()
    chain_block_reward = result['Ret']
    log.info("Block rewards on the chain： {}".format(chain_block_reward))
    result = client.ppos.getStakingReward()
    chain_pledge_reward = result['Ret']
    log.info("Pledge rewards on the chain：{}".format(chain_pledge_reward))
    assert per_block_reward == chain_block_reward, "ErrMsg:Block reward for the first settlement cycle {}".format(per_block_reward)
    assert staking_reward == chain_pledge_reward, "ErrMsg:Pledge rewards for the first settlement cycle {}".format(staking_reward)


def test_AL_NBI_020(client_consensus):
    """
    调整出块间隔，查看第二个结算周期出块奖励和质押奖励
    :param client_consensus:
    :return:
    """
    client = client_consensus
    economic = client.economic
    node = client.node
    economic.env.deploy_all()
    economic.wait_consensus_blocknum()
    log.info("Start adjusting the block interval")
    for i in range(3):
        economic.env.stop_all()
        time.sleep(2)
        economic.env.start_nodes(economic.env.get_all_nodes(), False)
        time.sleep(5)
    current_settlement_block_height = economic.get_settlement_switchpoint(node)
    log.info("Block height of current settlement cycle： {}".format(current_settlement_block_height))
    wait_block_number(node, current_settlement_block_height)
    incentive_pool_balance = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS, current_settlement_block_height)
    log.info("Settlement block high incentive pool balance： {}".format(incentive_pool_balance))
    block_info = node.eth.getBlock(1)
    first_timestamp = block_info['timestamp']
    log.info("First block timestamp： {}".format(first_timestamp))
    settlement_block_info = node.eth.getBlock(current_settlement_block_height)
    settlement_timestamp = settlement_block_info['timestamp']
    log.info("High block timestamp at the end of the current settlement cycle： {}".format(settlement_timestamp))
    issuing_cycle_timestamp = first_timestamp + (economic.additional_cycle_time * 60) * 1000
    log.info("End time stamp of current issue cycle： {}".format(issuing_cycle_timestamp))
    remaining_additional_time = issuing_cycle_timestamp - settlement_timestamp
    log.info("Remaining time of current issuance cycle： {}".format(remaining_additional_time))
    average_interval = (settlement_timestamp - first_timestamp) // (economic.settlement_size - 1)
    log.info("Block interval in the last settlement cycle： {}".format(average_interval))
    number_of_remaining_blocks = math.ceil(remaining_additional_time / average_interval)
    log.info("Remaining block height of current issuance cycle： {}".format(number_of_remaining_blocks))
    remaining_settlement_cycle = math.ceil(number_of_remaining_blocks / economic.settlement_size)
    log.info("remaining settlement cycles in the current issuance cycle： {}".format(remaining_settlement_cycle))
    per_block_reward, staking_reward = calculate_block_rewards_and_pledge_rewards(client, incentive_pool_balance, number_of_remaining_blocks, remaining_settlement_cycle)
    result = client.ppos.getPackageReward()
    chain_block_reward = result['Ret']
    log.info("Block rewards on the chain： {}".format(chain_block_reward))
    result = client.ppos.getStakingReward()
    chain_pledge_reward = result['Ret']
    log.info("Pledge rewards on the chain：{}".format(chain_pledge_reward))
    result = client.ppos.getAvgPackTime()
    chain_time_interval = result['Ret']
    log.info("链上出块间隔：{}".format(chain_time_interval))
    assert per_block_reward == chain_block_reward, "ErrMsg:Block rewards for the current settlement cycle {}".format(
        per_block_reward)
    assert staking_reward == chain_pledge_reward, "ErrMsg:Pledge rewards for the current settlement cycle {}".format(
        staking_reward)
    assert average_interval == chain_time_interval, "ErrMsg:Block interval in the last settlement cycle {}".format(average_interval)


def test_AL_FI_006(client_consensus):
    """
    增发周期动态调整
    :param client_consensus:
    :return:
    """
    client = client_consensus
    economic = client.economic
    node = client.node
    economic.env.deploy_all()
    log.info("Chain reset completed")
    economic.wait_consensus_blocknum()
    log.info("Start adjusting the block interval")
    for i in range(3):
        economic.env.stop_all()
        time.sleep(2)
        economic.env.start_nodes(economic.env.get_all_nodes(), False)
        time.sleep(5)
    remaining_settlement_cycle = (economic.additional_cycle_time * 60) // economic.settlement_size
    annual_size = remaining_settlement_cycle * economic.settlement_size
    log.info("Additional issue settlement period：{} Block height of current issuance cycle: {}".format(remaining_settlement_cycle, annual_size))
    economic.wait_settlement_blocknum(node)
    while remaining_settlement_cycle > 1:
        block_info = node.eth.getBlock(1)
        first_timestamp = block_info['timestamp']
        log.info("First block timestamp： {}".format(first_timestamp))
        tmp_current_block = node.eth.blockNumber
        if tmp_current_block % economic.settlement_size == 0:
            time.sleep(1)
        last_settlement_block = (math.ceil(tmp_current_block / economic.settlement_size) - 1) * economic.settlement_size
        log.info("The last block height of the previous settlement period： {}".format(last_settlement_block))
        settlement_block_info = node.eth.getBlock(last_settlement_block)
        settlement_timestamp = settlement_block_info['timestamp']
        log.info("High block timestamp at the end of the current settlement cycle： {}".format(settlement_timestamp))
        issuing_cycle_timestamp = first_timestamp + (economic.additional_cycle_time * 60) * 1000
        log.info("End time stamp of current issue cycle： {}".format(issuing_cycle_timestamp))
        remaining_additional_time = issuing_cycle_timestamp - settlement_timestamp
        log.info("Remaining time of current issuance cycle： {}".format(remaining_additional_time))
        average_interval = (settlement_timestamp - first_timestamp) // (economic.settlement_size - 1)
        log.info("Block interval in the last settlement cycle： {}".format(average_interval))
        number_of_remaining_blocks = math.ceil(remaining_additional_time / average_interval)
        log.info("Remaining block height of current issuance cycle： {}".format(number_of_remaining_blocks))
        remaining_settlement_cycle = math.ceil(number_of_remaining_blocks / economic.settlement_size)
        log.info("remaining settlement cycles in the current issuance cycle： {}".format(remaining_settlement_cycle))
        economic.wait_settlement_blocknum(node)
    initial_incentive_pool_amount = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS, 0)
    log.info("Incentive pool initial amount： {}".format(initial_incentive_pool_amount))
    plan_incentive_pool_amount = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS, annual_size)
    log.info("Incentive pool plan amount： {}".format(plan_incentive_pool_amount))
    assert initial_incentive_pool_amount == plan_incentive_pool_amount, "ErrMsg：Incentive pool balance {}".format(initial_incentive_pool_amount)
    current_increase_last_block = economic.get_settlement_switchpoint(node)
    log.info("The current issue cycle is high： {}".format(current_increase_last_block))
    economic.wait_settlement_blocknum()
    actual_incentive_pool_amount = node.eth.getBalance(EconomicConfig.INCENTIVEPOOL_ADDRESS, current_increase_last_block)
    log.info("Incentive pool actual amount： {}".format(actual_incentive_pool_amount))
    assert actual_incentive_pool_amount > plan_incentive_pool_amount, "ErrMsg：Incentive pool balance {}".format(initial_incentive_pool_amount)




