package network.platon.contracts.wasm;

import com.platon.rlp.datatypes.Int32;
import com.platon.rlp.datatypes.Int64;
import java.math.BigInteger;
import java.util.Arrays;
import org.web3j.abi.WasmFunctionEncoder;
import org.web3j.abi.datatypes.WasmFunction;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.WasmContract;
import org.web3j.tx.gas.GasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://github.com/PlatONnetwork/client-sdk-java/releases">platon-web3j command line tools</a>,
 * or the org.web3j.codegen.WasmFunctionWrapperGenerator in the 
 * <a href="https://github.com/PlatONnetwork/client-sdk-java/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with platon-web3j version 0.9.1.2-SNAPSHOT.
 */
public class ShellSort extends WasmContract {
    private static String BINARY_0 = "0x0061736d0100000001420c60017f0060027f7f0060017f017f60000060037f7f7f017f60037f7f7f0060027f7f017f60027f7e0060047f7f7f7f017f60047f7f7f7f0060017f017e6000017f02a9010703656e760c706c61746f6e5f70616e6963000303656e7617706c61746f6e5f6765745f696e7075745f6c656e677468000b03656e7610706c61746f6e5f6765745f696e707574000003656e760d706c61746f6e5f72657475726e000103656e7617706c61746f6e5f6765745f73746174655f6c656e677468000603656e7610706c61746f6e5f6765745f7374617465000803656e7610706c61746f6e5f7365745f73746174650009034b4a03000500060105040a030a0200010002020101000100070702020001010204060801000102010408000403040205030202000300030004020202000006090401050202000101010101070405017001030305030100020608017f0141d08a040b073904066d656d6f72790200115f5f7761736d5f63616c6c5f63746f727300070f5f5f66756e63735f6f6e5f65786974003906696e766f6b6500100908010041010b02083c0ad6594a080010311035103b0b070041940810380ba102020b7f027e2002210303400240200341026d210520034102480d004100210841002005410374220a6b210d200a210641002107034020052007460440200521030c03052001280200220b2109200721040340200420056a220c2002480440200b200c4103746a290300220e200b20044103746a290300530440200e422086422087210f200921030340024020044100480d00200320086a290300220e200f570d00200320066a200e3703002003200d6a2103200420056b21040c010b0b200320066a200f3703000b2009200a6a2109200c21040c010b0b200841086a2108200641086a2106200741016a21070c010b000b000b0b200041186a100a20002001290200370218200041206a200128020836020020014100360208200142003702000b2301017f2000280200220104402000200136020420004100360208200042003702000b0b3e01017f2000420037020020004100360208200128020420012802006b2202044020002002410375100c20012802002001280204200041046a100d0b20000b3001017f20014180808080024f0440000b2000200110242202360200200020023602042000200220014103746a3602080b2800200120006b220141014e044020022802002000200110321a2002200228020020016a3602000b0b3401017f230041106b220324002003200236020c200320013602082003200329030837030020002003411c103d200341106a24000b3901027e42a5c688a1c89ca7f94b210103402000300000220250450440200041016a2100200142b383808080207e20028521010c010b0b20010bd00202047f017e230041f0006b22002400100710012201103622021002200041206a200041086a20022001100e22014100104702400240200041206a10112204500d00418008100f2004510440200041206a101210130c020b418508100f20045104402000420037036020004200370358200041206a200141011047200041206a200041d8006a1014200041206a2001410210472000200041206a10112204420188420020044201837d853e0264200041206a1012200041206a200041c8006a200041d8006a100b220220002802641009200210151013200041d8006a10150c020b418a08100f2004520d00200041206a1012200041c8006a200041386a100b2102200041d8006a10162201200210171018200120021019200128020c200141106a28020047044010000b2001280200200128020410032001101a2002101510130c010b10000b1039200041f0006a24000b9e0202057f017e200010410240024020001048450d002000280204450d0020002802002d000041c001490d010b10000b200010402204200028020422024b04401000200028020421020b200028020021050240024002402002044020052c00002200417f4a0d01027f200041ff0171220141bf014d04404100200041ff017141b801490d011a200141c97e6a0c010b4100200041ff017141f801490d001a200141897e6a0b41016a21010c010b4101210120050d00410021000c010b410021002002200449200120046a20024b720d0020022001490d01200120056a2103200220016b20042004417f461b22004109490d0110000c010b0b0340200004402000417f6a210020033100002006420886842106200341016a21030c010b0b20060be803010b7f230041406a2202240020004200370218200042a5defba9b381cff0ac7f3703102000410036020820004200370200200041206a4100360200200241286a101622032000290310101d200328020c200341106a28020047044010000b200041186a210602402003280200220a2003280204220b10042204044020024100360220200242003703182004417f4c0d012004103721050340200120056a41003a00002004200141016a2201470d000b200120056a21072005200228021c200228021822086b22096b2101200941014e044020012008200910321a200228021821080b2002200420056a3602202002200736021c20022001360218027f4100200a200b20080440200228021c2107200228021821010b2001200720016b1005417f460d001a20022002280218220141016a200228021c2001417f736a100e2006101420040b2101200241186a10150b2003101a024020010d002000411c6a210120002802042204200028020022036b41037522072000280220200028021822056b4103754d04402007200128020020056b41037522064b04402003200320064103746a2203200510251a200320042001100d0c020b200120032004200510253602000c010b2006100a2006200620071026100c200320042001100d0b200241406b240020000f0b000ba503010c7f230041d0006b22022400200241186a10162104200241c8006a4100360200200241406b4200370300200241386a420037030020024200370330200241306a2000290310101e20022802302103200241306a410472102120042003101820042000290310101d200428020c200441106a28020047044010000b200428020421082004280200200241306a10162101200041186a220a1017210b41011037220341fe013a0000200220033602082002200341016a22053602102002200536020c200128020c200141106a2802004704401000200228020c2105200228020821030b2003210620012802042207200520036b22056a220c20012802084b04402001200c101b20012802042107200228020821060b200128020020076a2003200510321a2001200128020420056a3602042001200228020c200b20066b6a10182001200a10190240200128020c2001280210460440200128020021030c010b100020012802002103200128020c2001280210460d0010000b2008200320012802041006200241086a10152001101a2004101a200041186a101520001015200241d0006a24000be00202057f017e230041e0006b22022400024002402000280204450d0020002802002d000041c001490d002000104921032001280208200128020022046b41037520034904402001200241106a2003200128020420046b410375200141086a102722031028200310290b200241386a2000410110452103200141086a2105200241286a200041001045210403402003280204200428020446044020032802082004280208460d030b20022003290204220737034820022007370308200241106a200241086a411c103d10112207420188420020074201837d8521070240200128020422002001280208490440200020073703002001200041086a3602040c010b200241c8006a2001200020012802006b41037541016a1026200128020420012802006b4103752005102721002002280250220620073703002002200641086a360250200120001028200010290b200310420c000b000b10000b200241e0006a24000b1501017f200028020022010440200020013602040b0b2900200041003602082000420037020020004100101b200041146a41003602002000420037020c20000ba60102027f017e230041206b22012400200141186a4100360200200141106a4200370300200141086a420037030020014200370300027f200028020020002802044604402001410136020041010c010b20014100102a2000280204210220002802002100037f2000200246047f20014101102a2001280200052001200029030022034201862003423f8785101e200041086a21000c010b0b0b20014104721021200141206a24000b13002000280208200149044020002001101b0b0b4e02017f017e2000200128020420012802006b410375104e20012802042102200128020021010340200120024704402000200129030022034201862003423f8785101d200141086a21010c010b0b0b1c01017f200028020c22010440200041106a20013602000b2000101c0b3401017f200028020820014904402001103622022000280200200028020410321a2000101c20002001360208200020023602000b0b080020002802001a0b08002000200110500b7902017f017e4101210220014280015a044041002102034020012003845045044020034238862001420888842101200241016a2102200342088821030c010b0b200241384f047f2002101f20026a0520020b41016a21020b200041186a2802000440200041046a102021000b2000200028020020026a3602000b1e01017f03402000044020004108762100200141016a21010c010b0b20010b2e002000280204200028021420002802106a417f6a220041087641fcffff07716a280200200041ff07714102746a0b940201047f230041106b22042400200028020422012000280210220241087641fcffff07716a2103027f410020012000280208460d001a2003280200200241ff07714102746a0b2101200441086a20001022200428020c21020340024020012002460440200041003602142000280204210103402000280208220320016b41027522024103490d0220012802001a2000200028020441046a22013602040c000b000b200141046a220120032802006b418020470d0120032802042101200341046a21030c010b0b2002417f6a220241014d04402000418004418008200241016b1b3602100b03402001200347044020012802001a200141046a21010c010b0b20002000280204102320002802001a200441106a24000b4f01037f20012802042203200128021020012802146a220441087641fcffff07716a21022000027f410020032001280208460d001a2002280200200441ff07714102746a0b360204200020023602000b2501017f200028020821020340200120024645044020002002417c6a22023602080c010b0b0b160020004180808080024f0440000b200041037410370b2501017f200120006b220141037521032001044020022000200110340b200220034103746a0b3e01017f20014180808080024f0440000b2001200028020820002802006b2200410275220220022001491b41ffffffff01200041037541ffffffff00491b0b4c01017f2000410036020c200041106a2003360200200104402001102421040b200020043602002000200420024103746a22023602082000200420014103746a36020c2000200236020420000b870101037f200120012802042000280204200028020022046b22036b2202360204200341004a044020022004200310321a200128020421020b200028020021032000200236020020012003360204200028020421022000200128020836020420012002360208200028020821022000200128020c3602082001200236020c200120012802043602000b3101027f20002802082101200028020421020340200120024704402000200141786a22013602080c010b0b20002802001a0ba40c02077f027e230041306b22052400200041046a2107024020014101460440200710202802002101200041186a22022002280200417f6a3602002007102b4180104f04402000410c6a2202280200417c6a2802001a20072002280200417c6a10230b200141384f047f2001101f20016a0520010b41016a21012000280218450d012007102021000c010b02402007102b0d00200041146a28020022014180084f0440200020014180786a360214200041086a2201280200220228020021042001200241046a360200200520043602182007200541186a102c0c010b2000410c6a2802002202200041086a2802006b4102752204200041106a2203280200220620002802046b220141027549044041802010372104200220064704400240200028020c220120002802102206470d0020002802082202200028020422034b04402000200220012002200220036b41027541016a417e6d41027422036a102d220136020c2000200028020820036a3602080c010b200541186a200620036b2201410175410120011b22012001410276200041106a102e2102200028020c210320002802082101034020012003470440200228020820012802003602002002200228020841046a360208200141046a21010c010b0b200029020421092000200229020037020420022009370200200029020c21092000200229020837020c200220093702082002102f200028020c21010b200120043602002000200028020c41046a36020c0c020b02402000280208220120002802042206470d00200028020c2202200028021022034904402000200120022002200320026b41027541016a41026d41027422036a103022013602082000200028020c20036a36020c0c010b200541186a200320066b2201410175410120011b2201200141036a410276200041106a102e2102200028020c210320002802082101034020012003470440200228020820012802003602002002200228020841046a360208200141046a21010c010b0b200029020421092000200229020037020420022009370200200029020c21092000200229020837020c200220093702082002102f200028020821010b2001417c6a2004360200200020002802082201417c6a22023602082002280200210220002001360208200520023602182007200541186a102c0c010b20052001410175410120011b20042003102e210241802010372106024020022802082201200228020c2208470d0020022802042204200228020022034b04402002200420012004200420036b41027541016a417e6d41027422036a102d22013602082002200228020420036a3602040c010b200541186a200820036b2201410175410120011b22012001410276200241106a280200102e21042002280208210320022802042101034020012003470440200428020820012802003602002004200428020841046a360208200141046a21010c010b0b2002290200210920022004290200370200200420093702002002290208210920022004290208370208200420093702082004102f200228020821010b200120063602002002200228020841046a360208200028020c2104034020002802082004460440200028020421012000200228020036020420022001360200200228020421012002200436020420002001360208200029020c21092000200229020837020c200220093702082002102f052004417c6a210402402002280204220120022802002208470d0020022802082203200228020c22064904402002200120032003200620036b41027541016a41026d41027422066a103022013602042002200228020820066a3602080c010b200541186a200620086b2201410175410120011b2201200141036a4102762002280210102e2002280208210620022802042101034020012006470440200528022020012802003602002005200528022041046a360220200141046a21010c010b0b20022902002109200220052903183702002002290208210a20022005290320370208200520093703182005200a370320102f200228020421010b2001417c6a200428020036020020022002280204417c6a3602040c010b0b0b200541186a20071022200528021c4100360200200041186a2100410121010b2000200028020020016a360200200541306a24000b2801017f200028020820002802046b2201410874417f6a410020011b200028021420002802106a6b0ba10202057f017e230041206b22052400024020002802082202200028020c2206470d0020002802042203200028020022044b04402000200320022003200320046b41027541016a417e6d41027422046a102d22023602082000200028020420046a3602040c010b200541086a200620046b2202410175410120021b220220024102762000410c6a102e2103200028020821042000280204210203402002200446450440200328020820022802003602002003200328020841046a360208200241046a21020c010b0b2000290200210720002003290200370200200320073702002000290208210720002003290208370208200320073702082003102f200028020821020b200220012802003602002000200028020841046a360208200541206a24000b2501017f200120006b220141027521032001044020022000200110340b200220034102746a0b5f01017f2000410036020c200041106a200336020002402001044020014180808080044f0d012001410274103721040b200020043602002000200420024102746a22023602082000200420014102746a36020c2000200236020420000f0b000b3101027f200028020821012000280204210203402001200247044020002001417c6a22013602080c010b0b20002802001a0b1b00200120006b22010440200220016b22022000200110340b20020b3801017f4194084200370200419c08410036020041742100034020000440200041a0086a4100360200200041046a21000c010b0b4101103a0bf80801067f0340200020046a2105200120046a220341037145200220044672450440200520032d00003a0000200441016a21040c010b0b200220046b210602402005410371220845044003402006411049450440200020046a2202200120046a2203290200370200200241086a200341086a290200370200200441106a2104200641706a21060c010b0b027f2006410871450440200120046a2103200020046a0c010b200020046a2202200120046a2201290200370200200141086a2103200241086a0b21042006410471044020042003280200360200200341046a2103200441046a21040b20064102710440200420032f00003b0000200341026a2103200441026a21040b2006410171450d01200420032d00003a000020000f0b024020064120490d002008417f6a220841024b0d00024002400240024002400240200841016b0e020102000b2005200120046a220628020022033a0000200541016a200641016a2f00003b0000200041036a2108200220046b417d6a2106034020064111490d03200420086a2202200120046a220541046a2802002207410874200341187672360200200241046a200541086a2802002203410874200741187672360200200241086a2005410c6a28020022074108742003411876723602002002410c6a200541106a2802002203410874200741187672360200200441106a2104200641706a21060c000b000b2005200120046a220628020022033a0000200541016a200641016a2d00003a0000200041026a2108200220046b417e6a2106034020064112490d03200420086a2202200120046a220541046a2802002207411074200341107672360200200241046a200541086a2802002203411074200741107672360200200241086a2005410c6a28020022074110742003411076723602002002410c6a200541106a2802002203411074200741107672360200200441106a2104200641706a21060c000b000b2005200120046a28020022033a0000200041016a21082004417f7320026a2106034020064113490d03200420086a2202200120046a220541046a2802002207411874200341087672360200200241046a200541086a2802002203411874200741087672360200200241086a2005410c6a28020022074118742003410876723602002002410c6a200541106a2802002203411874200741087672360200200441106a2104200641706a21060c000b000b200120046a41036a2103200020046a41036a21050c020b200120046a41026a2103200020046a41026a21050c010b200120046a41016a2103200020046a41016a21050b20064110710440200520032d00003a00002005200328000136000120052003290005370005200520032f000d3b000d200520032d000f3a000f200541106a2105200341106a21030b2006410871044020052003290000370000200541086a2105200341086a21030b2006410471044020052003280000360000200541046a2105200341046a21030b20064102710440200520032f00003b0000200541026a2105200341026a21030b2006410171450d00200520032d00003a00000b20000bc90201037f200041003a000020004184026a2201417f6a41003a0000200041003a0002200041003a00012001417d6a41003a00002001417e6a41003a0000200041003a00032001417c6a41003a00002000410020006b41037122026a22014100360200200141840220026b417c7122036a2202417c6a4100360200024020034109490d002001410036020820014100360204200241786a4100360200200241746a410036020020034119490d002001410036021820014100360214200141003602102001410036020c200241706a41003602002002416c6a4100360200200241686a4100360200200241646a41003602002003200141047141187222036b2102200120036a2101034020024120490d0120014200370300200141186a4200370300200141106a4200370300200141086a4200370300200141206a2101200241606a21020c000b000b20000b8d0301037f024020002001460d00200120006b20026b410020024101746b4d044020002001200210321a0c010b20002001734103712103027f024020002001490440200020030d021a410021030340200120036a2104200020036a2205410371450440200220036b210241002103034020024104490d04200320056a200320046a280200360200200341046a21032002417c6a21020c000b000b20022003460d04200520042d00003a0000200341016a21030c000b000b024020030d002001417f6a21030340200020026a22044103714504402001417c6a21032000417c6a2104034020024104490d03200220046a200220036a2802003602002002417c6a21020c000b000b2002450d042004417f6a200220036a2d00003a00002002417f6a21020c000b000b2001417f6a210103402002450d03200020026a417f6a200120026a2d00003a00002002417f6a21020c000b000b200320046a2101200320056a0b210303402002450d01200320012d00003a00002002417f6a2102200341016a2103200141016a21010c000b000b0b3501017f230041106b220041d08a0436020c41bc0a200028020c41076a417871220036020041c00a200036020041c40a3f003602000b970101047f230041106b220124002001200036020c2000047f41c40a200041086a2202411076220041c40a2802006a220336020041c00a200241c00a28020022026a41076a417871220436020002400240200341107420044d044041c40a200341016a360200200041016a21000c010b2000450d010b200040000d0010000b20022001410c6a4104103241086a0541000b200141106a24000b0b002000410120001b10360b130020002d0000410171044020002802081a0b0b880101037f41a008410136020041a4082802002100034020000440034041a80841a8082802002201417f6a2202360200200141014845044041a0084100360200200020024102746a22004184016a280200200041046a28020011000041a008410136020041a40828020021000c010b0b41a808412036020041a408200028020022003602000c010b0b0b940101027f41a008410136020041a408280200220145044041a40841ac0836020041ac0821010b024041a8082802002202412046044041840210362201450d0120011033220141a40828020036020041a408200136020041a8084100360200410021020b41a808200241016a360200200120024102746a22014184016a4100360200200141046a20003602000b41a00841003602000b3801017f41b00a420037020041b80a410036020041742100034020000440200041bc0a6a4100360200200041046a21000c010b0b4102103a0b070041b00a10380b750020004200370210200042ffffffff0f3702082000200129020037020002402002410871450d002000103e20012802044f0d002002410471044010000c010b200042003702000b02402002411071450d002000103e20012802044d0d0020024104710440100020000f0b200042003702000b20000b2e01017f200028020445044041000f0b4101210120002802002c0000417f4c047f2000103f200010406a0541010b0b5b00027f027f41002000280204450d001a410020002802002c0000417f4a0d011a20002802002d0000220041bf014d04404100200041b801490d011a200041c97e6a0c010b4100200041f801490d001a200041897e6a0b41016a0b0bff0201037f200028020445044041000f0b2000104141012102024020002802002c00002201417f4a0d00200141ff0171220341b7014d0440200341807f6a0f0b02400240200141ff0171220141bf014d0440024020002802042201200341c97e6a22024d047f100020002802040520010b4102490d0020002802002d00010d0010000b200241054f044010000b20002802002d000145044010000b4100210241b7012101034020012003460440200241384f0d030c0405200028020020016a41ca7e6a2d00002002410874722102200141016a21010c010b000b000b200141f7014d0440200341c07e6a0f0b024020002802042201200341897e6a22024d047f100020002802040520010b4102490d0020002802002d00010d0010000b200241054f044010000b20002802002d000145044010000b4100210241f701210103402001200346044020024138490d0305200028020020016a418a7e6a2d00002002410874722102200141016a21010c010b0b0b200241ff7d490d010b10000b20020b4101017f200028020445044010000b0240200028020022012d0000418101470d00200028020441014d047f100020002802000520010b2c00014100480d0010000b0bb50102057f017e230041106b22022400200041046a210102402000280200220304402001280200220504402005200041086a2802006a21040b20002004360204200041086a2003360200200241086a20014100200420031043104420002002290308220637020420004100200028020022002006422088a76b2201200120004b1b3602000c010b200020012802002201047f2001200041086a2802006a0541000b360204200041086a41003602000b200241106a24000b3c01017f230041306b22022400200220013602142002200036021020022002290310370308200241186a200241086a4114103d103e200241306a24000b5a01027f2000027f0240200128020022054504400c010b200220036a200128020422014b2001200249720d00410020012003490d011a200220056a2104200120026b20032003417f461b0c010b41000b360204200020043602000be70101037f230041106b2204240020004200370200200041086a410036020020012802042103024002402002450440200321020c010b410021022003450d002003210220012802002d000041c001490d00200441086a2001104620004100200428020c2201200428020822022001104322032003417f461b20024520012003497222031b220536020820004100200220031b3602042000200120056b3602000c010b20012802002103200128020421012000410036020020004100200220016b20034520022001497222021b36020820004100200120036a20021b3602040b200441106a240020000b2101017f20011040220220012802044b044010000b200020012001103f200210440bd60202077f017e230041206b220324002001280208220420024b0440200341186a2001104620012003280218200328021c104336020c200341106a20011046410021042001027f410020032802102206450d001a410020032802142208200128020c2207490d001a200820072007417f461b210520060b360210200141146a2005360200200141003602080b200141106a210903400240200420024f0d002001280214450d00200341106a2001104641002104027f410020032802102207450d001a410020032802142208200128020c2206490d001a200820066b2104200620076a0b21052001200436021420012005360210200341106a20094100200520041043104420012003290310220a3702102001200128020c200a422088a76a36020c2001200128020841016a22043602080c010b0b20032009290200220a3703082003200a370300200020034114103d1a200341206a24000b980101037f200028020445044041000f0b20001041200028020022022c0000220141004e044020014100470f0b027f4101200141807f460d001a200141ff0171220341b7014d0440200028020441014d047f100020002802000520020b2d00014100470f0b4100200341bf014b0d001a2000280204200141ff017141ca7e6a22014d047f100020002802000520020b20016a2d00004100470b0b800101047f230041106b2201240002402000280204450d0020002802002d000041c001490d00200141086a20001046200128020c210003402000450d01200141002001280208220320032000104322046a20034520002004497222031b3602084100200020046b20031b2100200241016a21020c000b000b200141106a240020020bf50101057f0340024020002802102201200028020c460d00200141786a2802004504401000200028021021010b200141786a22022002280200417f6a220436020020040d002000200236021020004101200028020422032001417c6a28020022026b2201101f220441016a20014138491b220520036a104b200220002802006a220320056a200320011034200141374d0440200028020020026a200141406a3a00000c020b200441f7016a220341ff014d0440200028020020026a20033a00002000280200200220046a6a210203402001450d03200220013a0000200141087621012002417f6a21020c000b000510000c020b000b0b0b0f0020002001104c200020013602040b2f01017f2000280208200149044020011036200028020020002802041032210220002001360208200020023602000b0b1b00200028020420016a220120002802084b044020002001104c0b0b9f0201057f02402001044020002802042104200041106a2802002202200041146a280200220349044020022001ad2004ad422086843702002000200028021041086a3602100f0b027f41002002200028020c22026b410375220541016a2206200320026b2202410275220320032006491b41ffffffff01200241037541ffffffff00491b2202450d001a200241037410370b2103200320054103746a22052001ad2004ad4220868437020020052000280210200028020c22016b22046b2106200441014e044020062001200410321a200028020c21010b2000200320024103746a3602142000200541086a3602102000200636020c2001450d010f0b200041c001104f20004100104d200028020020002802046a4100410010321a2000104a0b0b250020004101104d200028020020002802046a20013a00002000200028020441016a3602040bb80202037f037e024020015004402000418001104f0c010b20014280015a044020012105034020052006845045044020064238862005420888842105200241016a2102200642088821060c010b0b0240200241384f04402002210303402003044020034108762103200441016a21040c010b0b200441c9004f044010000b2000200441b77f6a41ff0171104f2000200028020420046a104b200028020420002802006a417f6a21042002210303402003450d02200420033a0000200341087621032004417f6a21040c000b000b200020024180017341ff0171104f0b2000200028020420026a104b200028020420002802006a417f6a210203402001200784500d02200220013c0000200742388620014208888421012002417f6a2102200742088821070c000b000b20002001a741ff0171104f0b2000104a0b0b1a01004180080b13696e697400736f7274006765745f6172726179";

    public static String BINARY = BINARY_0;

    public static final String FUNC_SORT = "sort";

    public static final String FUNC_GET_ARRAY = "get_array";

    protected ShellSort(String contractAddress, Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected ShellSort(String contractAddress, Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<ShellSort> deploy(Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(ShellSort.class, web3j, credentials, contractGasProvider, encodedConstructor);
    }

    public static RemoteCall<ShellSort> deploy(Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(ShellSort.class, web3j, transactionManager, contractGasProvider, encodedConstructor);
    }

    public static RemoteCall<ShellSort> deploy(Web3j web3j, Credentials credentials, GasProvider contractGasProvider, BigInteger initialVonValue) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(ShellSort.class, web3j, credentials, contractGasProvider, encodedConstructor, initialVonValue);
    }

    public static RemoteCall<ShellSort> deploy(Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider, BigInteger initialVonValue) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(ShellSort.class, web3j, transactionManager, contractGasProvider, encodedConstructor, initialVonValue);
    }

    public RemoteCall<TransactionReceipt> sort(Int64[] arr, Int32 n) {
        final WasmFunction function = new WasmFunction(FUNC_SORT, Arrays.asList(arr,n), Void.class);
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> sort(Int64[] arr, Int32 n, BigInteger vonValue) {
        final WasmFunction function = new WasmFunction(FUNC_SORT, Arrays.asList(arr,n), Void.class);
        return executeRemoteCallTransaction(function, vonValue);
    }

    public RemoteCall<Int64[]> get_array() {
        final WasmFunction function = new WasmFunction(FUNC_GET_ARRAY, Arrays.asList(), Int64[].class);
        return executeRemoteCall(function, Int64[].class);
    }

    public static ShellSort load(String contractAddress, Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        return new ShellSort(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static ShellSort load(String contractAddress, Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        return new ShellSort(contractAddress, web3j, transactionManager, contractGasProvider);
    }
}
