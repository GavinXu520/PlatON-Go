package network.platon.contracts.wasm;

import com.platon.rlp.datatypes.Uint16;
import com.platon.rlp.datatypes.Uint8;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.web3j.abi.WasmEventEncoder;
import org.web3j.abi.WasmFunctionEncoder;
import org.web3j.abi.datatypes.WasmEvent;
import org.web3j.abi.datatypes.WasmEventParameter;
import org.web3j.abi.datatypes.WasmFunction;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.PlatonFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.WasmContract;
import org.web3j.tx.gas.GasProvider;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://github.com/PlatONnetwork/client-sdk-java/releases">platon-web3j command line tools</a>,
 * or the org.web3j.codegen.WasmFunctionWrapperGenerator in the 
 * <a href="https://github.com/PlatONnetwork/client-sdk-java/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with platon-web3j version 0.9.1.2-SNAPSHOT.
 */
public class ContractMigrate_new extends WasmContract {
    private static String BINARY_0 = "0x0061736d0100000001420c60017f017f60017f0060027f7f0060000060027f7e0060037f7f7f017f60037f7f7f0060047f7f7f7f0060027f7f017f6000017f60047f7f7f7f017f60017f017e02a9010703656e760c706c61746f6e5f70616e6963000303656e7617706c61746f6e5f6765745f696e7075745f6c656e677468000903656e7610706c61746f6e5f6765745f696e707574000103656e760d706c61746f6e5f72657475726e000203656e7617706c61746f6e5f6765745f73746174655f6c656e677468000803656e7610706c61746f6e5f6765745f7374617465000a03656e7610706c61746f6e5f7365745f73746174650007033a3903010200010101050b0302000000010002000201020400040002000002020305000603000001030103010500000001070802060001020202040405017001030305030100020608017f0141f08a040b073904066d656d6f72790200115f5f7761736d5f63616c6c5f63746f727300070f5f5f66756e63735f6f6e5f65786974002d06696e766f6b6500100908010041010b0208300a813f39080010251029102f0b070041b408102c0b13002000280208200149044020002001101b0b0b2900200041003602082000420037020020004100101b200041146a41003602002000420037020c20000b940201047f230041106b22042400200028020422012000280210220241087641fcffff07716a2103027f410020012000280208460d001a2003280200200241ff07714102746a0b2101200441086a20001023200428020c21020340024020012002460440200041003602142000280204210103402000280208220320016b41027522024103490d0220012802001a2000200028020441046a22013602040c000b000b200141046a220120032802006b418020470d0120032802042101200341046a21030c010b0b2002417f6a220241014d04402000418004418008200241016b1b3602100b03402001200347044020012802001a200141046a21010c010b0b20002000280204102420002802001a200441106a24000b1c01017f200028020c22010440200041106a20013602000b2000101a0b1501017f200028020022010440200020013602040b0b3401017f230041106b220324002003200236020c200320013602082003200329030837030020002003411c1031200341106a24000b3901027e42a5c688a1c89ca7f94b210103402000300000220250450440200041016a2100200142b383808080207e20028521010c010b0b20010bbe0402047f017e230041f0006b22002400100710012201102a22021002200041206a200041086a20022001100e220141001039200041206a103502400240200041206a103a450d002000280224450d0020002802202d000041c001490d010b10000b200041d8006a200041206a1011200028025c220241094f044010000b200028025821030340200204402002417f6a210220033100002004420886842104200341016a21030c010b0b024002402004500d00418008100f2004510440200041206a200141011039200041206a10122102200041206a200141021039200041206a10132103200041206a1014220120023a0010200141286a20033b0100200110150c020b418508100f2004510440200041206a200141011039200041206a10122101200041206a1014220220013a0010200210150c020b419108100f20045104402000200041206a101422022d001022033a0056200041d8006a100a2201200041d6006a10161009200120031017200128020c200141106a28020047044010000b2001280200200128020410032001100c200210150c020b419d08100f2004510440200041206a200141011039200041206a10132101200041206a1014220241286a20013b0100200210150c020b41a708100f2004520d002000200041206a1014220241286a2f010022033b0156200041d8006a100a2201200041d6006a10181009200120031017200128020c200141106a28020047044010000b2001280200200128020410032001100c200210150c010b10000b102d200041f0006a24000bd60101047f200110342204200128020422024b04401000200128020421020b20012802002105027f027f41002002450d001a410020052c00002203417f4a0d011a200341ff0171220141bf014d04404100200341ff017141b801490d011a200141c97e6a0c010b4100200341ff017141f801490d001a200141897e6a0b41016a0b21012000027f02402005450440410021030c010b410021032002200149200120046a20024b720d00410020022004490d011a200120056a2103200220016b20042004417f461b0c010b41000b360204200020033602000b7d01037f230041106b2201240020001035024002402000103a450d002000280204450d0020002802002d000041c001490d010b10000b200141086a20001011200128020c220041024f044010000b200128020821020340200004402000417f6a210020022d00002103200241016a21020c010b0b200141106a240020030b880101037f230041106b2201240020001035024002402000103a450d002000280204450d0020002802002d000041c001490d010b10000b200141086a20001011200128020c220041034f044010000b200128020821030340200004402000417f6a210020032d00002002410874722102200341016a21030c010b0b200141106a2400200241ffff03710b9d0301067f230041406a22012400200042d080f59381e7ada454370308200041003a0000200141286a100a22032000290308101c200328020c200341106a28020047044010000b20032802002204200328020422061004220504402001410036022020014200370318200141186a200510192004200620012802182204200128021c20046b1005417f470440200020012001280218220241016a200128021c2002417f736a100e10123a0010200521020b200141186a100d0b2003100c2002450440200020002d00003a00100b41002102200041003b0118200041206a220542e2e7e49581c7ebc754370300200141286a100a22032005290300101c200328020c200341106a28020047044010000b20032802002204200328020422061004220504402001410036022020014200370318200141186a200510192004200620012802182204200128021c20046b1005417f470440200020012001280218220241016a200128021c2002417f736a100e10133b0128200521020b200141186a100d0b2003100c2002450440200020002f01183b01280b200141406b240020000b940301087f230041406a22042400200441286a100a2202200041206a2201101d100920022001290300101c200228020c200241106a28020047044010000b200228020421062002280200200441106a100a2101200041286a22031018210820012004101f220510202001200820052802046a20052802006b1009200120032f010010170240200128020c200141106a280200460440200128020021030c010b100020012802002103200128020c2001280210460d0010000b20062003200128020410062005100d2001100c2002100c200441286a100a2202200041086a101d100920022000290308101c200228020c200241106a28020047044010000b200228020421052002280200200441106a100a2101200041106a22031016210720012004101f220010202001200720002802046a20002802006b1009200120032d000010170240200128020c200141106a280200460440200128020021030c010b100020012802002103200128020c2001280210460d0010000b20052003200128020410062000100d2001100c2002100c200441406b24000b4e01017f230041206b22012400200141186a4100360200200141106a4200370300200141086a42003703002001420037030020012000310000101e20012802002001410472100b200141206a24000b090020002001ad103f0b4e01017f230041206b22012400200141186a4100360200200141106a4200370300200141086a42003703002001420037030020012000330100101e20012802002001410472100b200141206a24000b910201067f0240024020002802042202200028020022056b220420014904402000280208220720026b200120046b22034f04400340200241003a00002000200028020441016a22023602042003417f6a22030d000c030b000b2001417f4c0d022001200720056b2202410174220520052001491b41ffffffff07200241ffffffff03491b220104402001102b21060b200420066a220421020340200241003a0000200241016a21022003417f6a22030d000b20042000280204200028020022036b22046b2105200441014e044020052003200410261a200028020021030b2000200120066a36020820002002360204200020053602002003450d010f0b200420014d0d002000200120056a3602040b0f0b000b080020002802001a0b3401017f200028020820014904402001102a22022000280200200028020410261a2000101a20002001360208200020023602000b0b080020002001103f0b4e01017f230041206b22012400200141186a4100360200200141106a4200370300200141086a42003703002001420037030020012000290300101e20012802002001410472100b200141206a24000b7502027f017e4101210320014280015a0440034020012004845045044020044238862001420888842101200241016a2102200442088821040c010b0b200241384f047f2002102120026a0520020b41016a21030b200041186a2802000440200041046a102221000b2000200028020020036a3602000b3a01017f200041003602082000420037020020004101102b2201360200200141fe013a00002000200141016a22013602082000200136020420000b6101037f200028020c200041106a28020047044010000b200028020422022001280204200128020022036b22016a220420002802084b047f20002004101b20002802040520020b20002802006a2003200110261a2000200028020420016a3602040b1e01017f03402000044020004108762100200141016a21010c010b0b20010b2e002000280204200028021420002802106a417f6a220041087641fcffff07716a280200200041ff07714102746a0b4f01037f20012802042203200128021020012802146a220441087641fcffff07716a21022000027f410020032001280208460d001a2002280200200441ff07714102746a0b360204200020023602000b2501017f200028020821020340200120024645044020002002417c6a22023602080c010b0b0b3801017f41b408420037020041bc08410036020041742100034020000440200041c0086a4100360200200041046a21000c010b0b4101102e0bf80801067f0340200020046a2105200120046a220341037145200220044672450440200520032d00003a0000200441016a21040c010b0b200220046b210602402005410371220845044003402006411049450440200020046a2202200120046a2203290200370200200241086a200341086a290200370200200441106a2104200641706a21060c010b0b027f2006410871450440200120046a2103200020046a0c010b200020046a2202200120046a2201290200370200200141086a2103200241086a0b21042006410471044020042003280200360200200341046a2103200441046a21040b20064102710440200420032f00003b0000200341026a2103200441026a21040b2006410171450d01200420032d00003a000020000f0b024020064120490d002008417f6a220841024b0d00024002400240024002400240200841016b0e020102000b2005200120046a220628020022033a0000200541016a200641016a2f00003b0000200041036a2108200220046b417d6a2106034020064111490d03200420086a2202200120046a220541046a2802002207410874200341187672360200200241046a200541086a2802002203410874200741187672360200200241086a2005410c6a28020022074108742003411876723602002002410c6a200541106a2802002203410874200741187672360200200441106a2104200641706a21060c000b000b2005200120046a220628020022033a0000200541016a200641016a2d00003a0000200041026a2108200220046b417e6a2106034020064112490d03200420086a2202200120046a220541046a2802002207411074200341107672360200200241046a200541086a2802002203411074200741107672360200200241086a2005410c6a28020022074110742003411076723602002002410c6a200541106a2802002203411074200741107672360200200441106a2104200641706a21060c000b000b2005200120046a28020022033a0000200041016a21082004417f7320026a2106034020064113490d03200420086a2202200120046a220541046a2802002207411874200341087672360200200241046a200541086a2802002203411874200741087672360200200241086a2005410c6a28020022074118742003410876723602002002410c6a200541106a2802002203411874200741087672360200200441106a2104200641706a21060c000b000b200120046a41036a2103200020046a41036a21050c020b200120046a41026a2103200020046a41026a21050c010b200120046a41016a2103200020046a41016a21050b20064110710440200520032d00003a00002005200328000136000120052003290005370005200520032f000d3b000d200520032d000f3a000f200541106a2105200341106a21030b2006410871044020052003290000370000200541086a2105200341086a21030b2006410471044020052003280000360000200541046a2105200341046a21030b20064102710440200520032f00003b0000200541026a2105200341026a21030b2006410171450d00200520032d00003a00000b20000bc90201037f200041003a000020004184026a2201417f6a41003a0000200041003a0002200041003a00012001417d6a41003a00002001417e6a41003a0000200041003a00032001417c6a41003a00002000410020006b41037122026a22014100360200200141840220026b417c7122036a2202417c6a4100360200024020034109490d002001410036020820014100360204200241786a4100360200200241746a410036020020034119490d002001410036021820014100360214200141003602102001410036020c200241706a41003602002002416c6a4100360200200241686a4100360200200241646a41003602002003200141047141187222036b2102200120036a2101034020024120490d0120014200370300200141186a4200370300200141106a4200370300200141086a4200370300200141206a2101200241606a21020c000b000b20000b8d0301037f024020002001460d00200120006b20026b410020024101746b4d044020002001200210261a0c010b20002001734103712103027f024020002001490440200020030d021a410021030340200120036a2104200020036a2205410371450440200220036b210241002103034020024104490d04200320056a200320046a280200360200200341046a21032002417c6a21020c000b000b20022003460d04200520042d00003a0000200341016a21030c000b000b024020030d002001417f6a21030340200020026a22044103714504402001417c6a21032000417c6a2104034020024104490d03200220046a200220036a2802003602002002417c6a21020c000b000b2002450d042004417f6a200220036a2d00003a00002002417f6a21020c000b000b2001417f6a210103402002450d03200020026a417f6a200120026a2d00003a00002002417f6a21020c000b000b200320046a2101200320056a0b210303402002450d01200320012d00003a00002002417f6a2102200341016a2103200141016a21010c000b000b0b3501017f230041106b220041f08a0436020c41dc0a200028020c41076a417871220036020041e00a200036020041e40a3f003602000b970101047f230041106b220124002001200036020c2000047f41e40a200041086a2202411076220041e40a2802006a220336020041e00a200241e00a28020022026a41076a417871220436020002400240200341107420044d044041e40a200341016a360200200041016a21000c010b2000450d010b200040000d0010000b20022001410c6a4104102641086a0541000b200141106a24000b0b002000410120001b102a0b130020002d0000410171044020002802081a0b0b880101037f41c008410136020041c4082802002100034020000440034041c80841c8082802002201417f6a2202360200200141014845044041c0084100360200200020024102746a22004184016a280200200041046a28020011010041c008410136020041c40828020021000c010b0b41c808412036020041c408200028020022003602000c010b0b0b940101027f41c008410136020041c408280200220145044041c40841cc0836020041cc0821010b024041c80828020022024120460440418402102a2201450d0120011027220141c40828020036020041c408200136020041c8084100360200410021020b41c808200241016a360200200120024102746a22014184016a4100360200200141046a20003602000b41c00841003602000b3801017f41d00a420037020041d80a410036020041742100034020000440200041dc0a6a4100360200200041046a21000c010b0b4102102e0b070041d00a102c0b750020004200370210200042ffffffff0f3702082000200129020037020002402002410871450d002000103220012802044f0d002002410471044010000c010b200042003702000b02402002411071450d002000103220012802044d0d0020024104710440100020000f0b200042003702000b20000b2e01017f200028020445044041000f0b4101210120002802002c0000417f4c047f20001033200010346a0541010b0b5b00027f027f41002000280204450d001a410020002802002c0000417f4a0d011a20002802002d0000220041bf014d04404100200041b801490d011a200041c97e6a0c010b4100200041f801490d001a200041897e6a0b41016a0b0bff0201037f200028020445044041000f0b2000103541012102024020002802002c00002201417f4a0d00200141ff0171220341b7014d0440200341807f6a0f0b02400240200141ff0171220141bf014d0440024020002802042201200341c97e6a22024d047f100020002802040520010b4102490d0020002802002d00010d0010000b200241054f044010000b20002802002d000145044010000b4100210241b7012101034020012003460440200241384f0d030c0405200028020020016a41ca7e6a2d00002002410874722102200141016a21010c010b000b000b200141f7014d0440200341c07e6a0f0b024020002802042201200341897e6a22024d047f100020002802040520010b4102490d0020002802002d00010d0010000b200241054f044010000b20002802002d000145044010000b4100210241f701210103402001200346044020024138490d0305200028020020016a418a7e6a2d00002002410874722102200141016a21010c010b0b0b200241ff7d490d010b10000b20020b4101017f200028020445044010000b0240200028020022012d0000418101470d00200028020441014d047f100020002802000520010b2c00014100480d0010000b0b5a01027f2000027f0240200128020022054504400c010b200220036a200128020422014b2001200249720d00410020012003490d011a200220056a2104200120026b20032003417f461b0c010b41000b360204200020043602000b3c01017f230041306b22022400200220013602142002200036021020022002290310370308200241186a200241086a411410311032200241306a24000b2101017f20011034220220012802044b044010000b2000200120011033200210360bd60202077f017e230041206b220324002001280208220420024b0440200341186a2001103820012003280218200328021c103736020c200341106a20011038410021042001027f410020032802102206450d001a410020032802142208200128020c2207490d001a200820072007417f461b210520060b360210200141146a2005360200200141003602080b200141106a210903400240200420024f0d002001280214450d00200341106a2001103841002104027f410020032802102207450d001a410020032802142208200128020c2206490d001a200820066b2104200620076a0b21052001200436021420012005360210200341106a20094100200520041037103620012003290310220a3702102001200128020c200a422088a76a36020c2001200128020841016a22043602080c010b0b20032009290200220a3703082003200a37030020002003411410311a200341206a24000b980101037f200028020445044041000f0b20001035200028020022022c0000220141004e044020014100470f0b027f4101200141807f460d001a200141ff0171220341b7014d0440200028020441014d047f100020002802000520020b2d00014100470f0b4100200341bf014b0d001a2000280204200141ff017141ca7e6a22014d047f100020002802000520020b20016a2d00004100470b0bf80101057f0340024020002802102201200028020c460d00200141786a28020041014904401000200028021021010b200141786a2202200228020041016b220436020020040d002000200236021020004101200028020422032001417c6a28020022026b22011021220441016a20014138491b220520036a103c200220002802006a220320056a2003200110280240200141374d0440200028020020026a200141406a3a00000c010b200441f7016a220341ff014d0440200028020020026a20033a00002000280200200220046a6a210203402001450d02200220013a0000200141087621012002417f6a21020c000b000b10000b0c010b0b0b0f0020002001103d200020013602040b2f01017f200028020820014904402001102a200028020020002802041026210220002001360208200020023602000b0b3a01017f200028020441016a220220002802084b044020002002103d0b200028020020002802046a20013a00002000200028020441016a3602040bb80202037f037e024020015004402000418001103e0c010b20014280015a044020012105034020052006845045044020064238862005420888842105200241016a2102200642088821060c010b0b0240200241384f04402002210303402003044020034108762103200441016a21040c010b0b200441c9004f044010000b2000200441b77f6a41ff0171103e2000200028020420046a103c200028020420002802006a417f6a21042002210303402003450d02200420033a0000200341087621032004417f6a21040c000b000b200020024180017341ff0171103e0b2000200028020420026a103c200028020420002802006a417f6a210203402001200784500d02200220013c0000200742388620014208888421012002417f6a2102200742088821070c000b000b20002001a741ff0171103e0b2000103b0b0b3701004180080b30696e69740073657455696e74384e65770067657455696e74384e65770073657455696e7431360067657455696e743136";

    public static String BINARY = BINARY_0;

    public static final String FUNC_SETUINT8NEW = "setUint8New";

    public static final String FUNC_GETUINT8NEW = "getUint8New";

    public static final String FUNC_SETUINT16 = "setUint16";

    public static final String FUNC_GETUINT16 = "getUint16";

    public static final WasmEvent TRANSFER_EVENT = new WasmEvent("transfer", Arrays.asList(new WasmEventParameter(String.class, true)), Arrays.asList(new WasmEventParameter(String.class)));
    ;

    protected ContractMigrate_new(String contractAddress, Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected ContractMigrate_new(String contractAddress, Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        List<WasmContract.WasmEventValuesWithLog> valueList = extractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (WasmContract.WasmEventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.topic = (String) eventValues.getIndexedValues().get(0);
            typedResponse.arg1 = (String) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TransferEventResponse> transferEventObservable(PlatonFilter filter) {
        return web3j.platonLogObservable(filter).map(new Func1<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse call(Log log) {
                WasmContract.WasmEventValuesWithLog eventValues = extractEventParametersWithLog(TRANSFER_EVENT, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse.topic = (String) eventValues.getIndexedValues().get(0);
                typedResponse.arg1 = (String) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Observable<TransferEventResponse> transferEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        PlatonFilter filter = new PlatonFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(WasmEventEncoder.encode(TRANSFER_EVENT));
        return transferEventObservable(filter);
    }

    public static RemoteCall<ContractMigrate_new> deploy(Web3j web3j, Credentials credentials, GasProvider contractGasProvider, Uint8 input, Uint16 input2) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList(input,input2));
        return deployRemoteCall(ContractMigrate_new.class, web3j, credentials, contractGasProvider, encodedConstructor);
    }

    public static RemoteCall<ContractMigrate_new> deploy(Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider, Uint8 input, Uint16 input2) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList(input,input2));
        return deployRemoteCall(ContractMigrate_new.class, web3j, transactionManager, contractGasProvider, encodedConstructor);
    }

    public static RemoteCall<ContractMigrate_new> deploy(Web3j web3j, Credentials credentials, GasProvider contractGasProvider, BigInteger initialVonValue, Uint8 input, Uint16 input2) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList(input,input2));
        return deployRemoteCall(ContractMigrate_new.class, web3j, credentials, contractGasProvider, encodedConstructor, initialVonValue);
    }

    public static RemoteCall<ContractMigrate_new> deploy(Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider, BigInteger initialVonValue, Uint8 input, Uint16 input2) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList(input,input2));
        return deployRemoteCall(ContractMigrate_new.class, web3j, transactionManager, contractGasProvider, encodedConstructor, initialVonValue);
    }

    public RemoteCall<TransactionReceipt> setUint8New(Uint8 input) {
        final WasmFunction function = new WasmFunction(FUNC_SETUINT8NEW, Arrays.asList(input), Void.class);
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setUint8New(Uint8 input, BigInteger vonValue) {
        final WasmFunction function = new WasmFunction(FUNC_SETUINT8NEW, Arrays.asList(input), Void.class);
        return executeRemoteCallTransaction(function, vonValue);
    }

    public RemoteCall<Uint8> getUint8New() {
        final WasmFunction function = new WasmFunction(FUNC_GETUINT8NEW, Arrays.asList(), Uint8.class);
        return executeRemoteCall(function, Uint8.class);
    }

    public RemoteCall<TransactionReceipt> setUint16(Uint16 input) {
        final WasmFunction function = new WasmFunction(FUNC_SETUINT16, Arrays.asList(input), Void.class);
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setUint16(Uint16 input, BigInteger vonValue) {
        final WasmFunction function = new WasmFunction(FUNC_SETUINT16, Arrays.asList(input), Void.class);
        return executeRemoteCallTransaction(function, vonValue);
    }

    public RemoteCall<Uint16> getUint16() {
        final WasmFunction function = new WasmFunction(FUNC_GETUINT16, Arrays.asList(), Uint16.class);
        return executeRemoteCall(function, Uint16.class);
    }

    public static ContractMigrate_new load(String contractAddress, Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        return new ContractMigrate_new(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static ContractMigrate_new load(String contractAddress, Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        return new ContractMigrate_new(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class TransferEventResponse {
        public Log log;

        public String topic;

        public String arg1;
    }
}
