package network.platon.contracts.wasm;

import com.platon.rlp.datatypes.Uint8;
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
public class OneInherit extends WasmContract {
    private static String BINARY_0 = "0x0061736d0100000001420c60027f7f0060017f0060017f017f60027f7f017f60037f7f7f0060037f7f7f017f60000060047f7f7f7f0060027f7e0060047f7f7f7f017f6000017f60017f017e02a9010703656e760c706c61746f6e5f70616e6963000603656e760d706c61746f6e5f72657475726e000003656e7617706c61746f6e5f6765745f696e7075745f6c656e677468000a03656e7610706c61746f6e5f6765745f696e707574000103656e7617706c61746f6e5f6765745f73746174655f6c656e677468000303656e7610706c61746f6e5f6765745f7374617465000903656e7610706c61746f6e5f7365745f73746174650007035b5a060101010003030900010404050b000002020301000301010602000108010001080202000002000504000100030001020005090105050304060202030401000601060105020202010103070500040202070000000000000003080405017001050505030100020608017f0141908b040b073904066d656d6f72790200115f5f7761736d5f63616c6c5f63746f727300070f5f5f66756e63735f6f6e5f65786974004606696e766f6b65001f090a010041010b0411120a490ad56c5a0f0041d008100841031047103f10480b170020004200370200200041086a4100360200200010090b2201017f03402001410c470440200020016a4100360200200141046a21010c010b0b0b070041d00810440b940101047f230041206b2202240002402000411c6a2802002203200041206a220428020047044020032001100c1a2000200028021c41246a36021c0c010b200241086a200041186a2205200320002802186b41246d41016a100d200028021c20002802186b41246d2004100e22002802082001100c1a2000200028020841246a36020820052000100f200010100b200241206a24000b25002000200110421a2000410c6a2001410c6a10421a200041186a200141186a10421a20000b3b01017f200141c8e3f1384f0440000b2001200028020820002802006b41246d2200410174220220022001491b41c7e3f138200041e3f1b81c491b0b4c01017f2000410036020c200041106a2003360200200104402001102c21040b2000200436020020002004200241246c6a220236020820002004200141246c6a36020c2000200236020420000b900101027f2000280204210220002802002103034020022003464504402001280204415c6a2002415c6a2202102d20012001280204415c6a3602040c010b0b200028020021022000200128020436020020012002360204200028020421022000200128020836020420012002360208200028020821022000200128020c3602082001200236020c200120012802043602000b3301027f20002802042102034020022000280208220147044020002001415c6a2201360208200110220c010b0b20002802001a0b120020002001280218200241246c6a10421a0b150020002001280218200241246c6a410c6a10421a0b3401017f230041106b220324002003200236020c200320013602082003200329030837030020002003411c104a200341106a24000b3901027e42a5c688a1c89ca7f94b210103402000300000220250450440200041016a2100200142b383808080207e20028521010c010b0b20010be40201047f23004180016b22022400200128020421042001280200210320022000410110542002104e0240024020021055450d002002280204450d0020022802002d000041c001490d010b10000b200241e0006a200210162002280264220141024f044010000b200228026021000340200104402001417f6a210120002d00002105200041016a21000c010b0b20021017200241286a200220044101756a220020052004410171047f200028020020036a2802000520030b110400200241386a10182100200241f8006a4100360200200241f0006a4200370300200241e8006a420037030020024200370360200241e0006a200241d0006a200241286a104222031019200310442002280260210341046a101a20002003101b2000200241e0006a200241286a10422203101c1a20031044200028020c200041106a28020047044010000b2000280200200028020410012000101d200241286a1044101e20024180016a24000bd60101047f2001104d2204200128020422024b04401000200128020421020b20012802002105027f027f41002002450d001a410020052c00002203417f4a0d011a200341ff0171220141bf014d04404100200341ff017141b801490d011a200141c97e6a0c010b4100200341ff017141f801490d001a200141897e6a0b41016a0b21012000027f02402005450440410021030c010b410021032002200149200120046a20024b720d00410020022004490d011a200120056a2103200220016b20042004417f461b0c010b41000b360204200020033602000bf906020c7f017e230041c0016b2202240020004200370218200042c5cb94caeffff0b2a67f3703102000410036020820004200370200200041206a4100360200200241306a1018220720002903101027200728020c200741106a28020047044010000b200041186a21052000411c6a2109024002402007280200220b2007280204220c10042204044020024100360228200242003703202004417f4c0d012004104121030340200120036a41003a00002004200141016a2201470d000b200120036a210820032002280224200228022022066b220a6b2101200a41014e044020012006200a103c1a200228022021060b2002200320046a36022820022008360224200220013602200240200b200c2006044020022802242108200228022021010b2001200820016b1005417f460440410021040c010b0240200241086a2002280220220141016a20022802242001417f736a10132201280204450d0020012802002d000041c001490d002001105621032000280220200028021822086b41246d20034904402005200241c8006a2003200028021c20086b41246d200041206a100e2203100f200310100b20024198016a2001410110522103200041206a210a20024188016a200141001052210803402003280204200828020446044020032802082008280208460d030b20022003290204220d3703482002200d370300200241f0006a2002411c104a200241c8006a1020220110210240200028021c2206200028022049044020062001102d2009200928020041246a3602000c010b200241a8016a2005200620052802006b41246d41016a100d200928020020052802006b41246d200a100e210620022802b0012001102d200220022802b00141246a3602b00120052006100f200610100b200110222003104f0c000b000b10000b200241206a1024200421010b2007101d024020010d0020002802042207200028020022016b41246d22032000280220200028021822046b41246d4d04402003200928020020046b41246d22034b044020012001200341246c6a22012004102e1a200120072009102f0c020b2005200120072004102e10300c010b200404402005103120002802181a20004100360220200042003702180b20052003100d220441c8e3f1384f0d0220002004102c22053602182000200536021c20002005200441246c6a360220200120072009102f0b200241c0016a240020000f0b000b000b29002000410036020820004200370200200041001025200041146a41003602002000420037020c20000b900101037f410121030240200128020420012d00002202410176200241017122041b2202450d0002400240200241014604402001280208200141016a20041b2c0000417f4c0d010c030b200241374b0d010b200241016a21030c010b2002102820026a41016a21030b027f200041186a2802000440200041046a10290c010b20000b2201200128020020036a36020020000b940201047f230041106b22042400200028020422012000280210220241087641fcffff07716a2103027f410020012000280208460d001a2003280200200241ff07714102746a0b2101200441086a2000102a200428020c21020340024020012002460440200041003602142000280204210103402000280208220320016b41027522024103490d0220012802001a2000200028020441046a22013602040c000b000b200141046a220120032802006b418020470d0120032802042101200341046a21030c010b0b2002417f6a220241014d04402000418004418008200241016b1b3602100b03402001200347044020012802001a200141046a21010c010b0b20002000280204102b20002802001a200441106a24000b1300200028020820014904402000200110250b0b5201037f230041106b2202240020022001280208200141016a20012d0000220341017122041b36020820022001280204200341017620041b36020c2002200229030837030020002002105f200241106a24000b1c01017f200028020c22010440200041106a20013602000b200010260baa07010d7f230041c0016b22012400200141286a10182108200141b8016a22034100360200200141b0016a22064200370300200141a8016a22044200370300200142003703a001200141a0016a2000290310102320012802a0012102200141a0016a410472101a20082002101b200820002903101027200828020c200841106a28020047044010000b2008280204210a2008280200200141106a10182102200341003602002006420037030020044200370300200142003703a001027f20002802182000411c6a280200460440200141013602a00141010c010b200141a0016a410010332106200028021c200028021822036b2104037f2004047f2006410010332207200310342007200141e0006a2003410c6a10422207101920014190016a200341186a1042220910192009104420071044410110331a2004415c6a2104200341246a21030c01052006410110331a20012802a0010b0b0b2109200141a0016a410472101a41011041220341fe013a0000200120033602002001200341016a220436020820012004360204200228020c200241106a280200470440100020012802042104200128020021030b2003210620022802042207200420036b22046a220520022802084b044020022005102520022802042107200128020021060b200228020020076a20032004103c1a2002200228020420046a36020420022001280204200920066b6a101b2002200028021c20002802186b41246d105c200028021c200028021822036b2104200141e0006a4104722106200141a0016a410472210703402004044020024103105c20014100360278200142003703702001420037036820014200370360200141e0006a20031034200141e0006a200141d0006a2003410c6a2209104222051019200141406b200341186a220c1042220d10191a200d10442005104420022001280260101b20024101105c200141003602b801200142003703b001200142003703a801200142003703a001200141a0016a20014190016a20031042220510191a20051044200220012802a001101b200220014180016a200310422205101c1a200510442007101a2002200141a0016a200910422209101c20014190016a200c10422205101c1a20051044200910442006101a2004415c6a2104200341246a21030c010b0b0240200228020c2002280210460440200228020021030c010b100020022802002103200228020c2002280210460d0010000b200a200320022802041006200110242002101d2008101d200041186a103520001035200141c0016a24000bc20402057f017e230041a0016b22002400100710022201104022021003200041f8006a200041106a200220011013220141001054200041f8006a104e02400240200041f8006a1055450d00200028027c450d0020002802782d000041c001490d010b10000b200041d0006a200041f8006a10162000280254220241094f044010000b200028025021030340200204402002417f6a210220033100002005420886842105200341016a21030c010b0b024002402005500d0041800810142005510440200041f8006a1017101e0c020b41850810142005510440200041286a4124103d1a200041286a10202102200041f8006a200141011054200041f8006a20021021200041f8006a1017200041f8006a200041d0006a2002100c2203100b20031022101e200210220c020b41940810142005510440200041f8006a101720004194016a28020021032000280290012104200041286a10182101200041e8006a4100360200200041e0006a4200370300200041d8006a420037030020004200370350200041d0006a200320046b41246d41ff0171ad2205102320002802502103200041d0006a410472101a20012003101b200120051060200128020c200141106a28020047044010000b2001280200200128020410012001101d101e0c020b41a808101420055104402000410036027c20004101360278200020002903783703002001200010150c020b41bc0810142005520d002000410036027c20004102360278200020002903783703082001200041086a10150c010b10000b1046200041a0016a24000b1600200010082000410c6a1008200041186a100820000b5e01017f230041306b220224002002200041001054200241186a200241001054200241186a20011032200241186a200041011054200241186a2001410c6a1032200241186a200041021054200241186a200141186a1032200241306a24000b1400200041186a10442000410c6a1044200010440b7502027f017e4101210320014280015a0440034020012004845045044020044238862001420888842101200241016a2102200442088821040c010b0b200241384f047f2002102820026a0520020b41016a21030b200041186a2802000440200041046a102921000b2000200028020020036a3602000b1501017f200028020022010440200020013602040b0b3401017f2000280208200149044020011040220220002802002000280204103c1a2000102620002001360208200020023602000b0b080020002802001a0b08002000200110600b1e01017f03402000044020004108762100200141016a21010c010b0b20010b2e002000280204200028021420002802106a417f6a220041087641fcffff07716a280200200041ff07714102746a0b4f01037f20012802042203200128021020012802146a220441087641fcffff07716a21022000027f410020032001280208460d001a2002280200200441ff07714102746a0b360204200020023602000b2501017f200028020821020340200120024645044020002002417c6a22023602080c010b0b0b1500200041c8e3f1384f0440000b200041246c10410b620020002001290200370200200041086a200141086a28020036020020011009200041146a200141146a2802003602002000200129020c37020c2001410c6a1009200041206a200141206a28020036020020002001290218370218200141186a10090b4800200120006b21010340200104402002200010452002410c6a2000410c6a1045200241186a200041186a1045200241246a21022001415c6a2101200041246a21000c010b0b20020b2e000340200020014645044020022802002000100c1a2002200228020041246a360200200041246a21000c010b0b0b2901017f20002802042102034020012002464504402002415c6a220210220c010b0b200020013602040b0b002000200028020010300b8b0301057f230041206b220224000240024002402000280204044020002802002d000041c001490d010b200241086a10080c010b200241186a200010162000104d21030240024002400240200228021822000440200228021c220420034f0d010b41002100200241106a410036020020024200370308410021030c010b200241106a410036020020024200370308200420032003417f461b220341704f0d04200020036a21052003410a4b0d010b200220034101743a0008200241086a41017221040c010b200341106a4170712206104121042002200336020c20022006410172360208200220043602100b034020002005470440200420002d00003a0000200441016a2104200041016a21000c010b0b200441003a00000b024020012d0000410171450440200141003b01000c010b200128020841003a00002001410036020420012d0000410171450d0020012802081a200141003602000b20012002290308370200200141086a200241106a280200360200200241086a1009200241086a1044200241206a24000f0b000ba60c02077f027e230041306b22052400200041046a2107027f20014101460440200710292802002101200041186a22022002280200417f6a360200200710364180104f04402000410c6a2202280200417c6a2802001a20072002280200417c6a102b0b200141384f047f2001102820016a0520010b41016a210120002802180440200710290c020b20000c010b0240200710360d00200041146a28020022014180084f0440200020014180786a360214200041086a2201280200220228020021042001200241046a360200200520043602182007200541186a10370c010b2000410c6a2802002202200041086a2802006b4102752204200041106a2203280200220620002802046b220141027549044041802010412104200220064704400240200028020c220120002802102206470d0020002802082202200028020422034b04402000200220012002200220036b41027541016a417e6d41027422036a1038220136020c2000200028020820036a3602080c010b200541186a200620036b2201410175410120011b22012001410276200041106a10392102200028020c210320002802082101034020012003470440200228020820012802003602002002200228020841046a360208200141046a21010c010b0b200029020421092000200229020037020420022009370200200029020c21092000200229020837020c200220093702082002103a200028020c21010b200120043602002000200028020c41046a36020c0c020b02402000280208220120002802042206470d00200028020c2202200028021022034904402000200120022002200320026b41027541016a41026d41027422036a103b22013602082000200028020c20036a36020c0c010b200541186a200320066b2201410175410120011b2201200141036a410276200041106a10392102200028020c210320002802082101034020012003470440200228020820012802003602002002200228020841046a360208200141046a21010c010b0b200029020421092000200229020037020420022009370200200029020c21092000200229020837020c200220093702082002103a200028020821010b2001417c6a2004360200200020002802082201417c6a22023602082002280200210220002001360208200520023602182007200541186a10370c010b20052001410175410120011b200420031039210241802010412106024020022802082201200228020c2208470d0020022802042204200228020022034b04402002200420012004200420036b41027541016a417e6d41027422036a103822013602082002200228020420036a3602040c010b200541186a200820036b2201410175410120011b22012001410276200241106a280200103921042002280208210320022802042101034020012003470440200428020820012802003602002004200428020841046a360208200141046a21010c010b0b2002290200210920022004290200370200200420093702002002290208210920022004290208370208200420093702082004103a200228020821010b200120063602002002200228020841046a360208200028020c2104034020002802082004460440200028020421012000200228020036020420022001360200200228020421012002200436020420002001360208200029020c21092000200229020837020c200220093702082002103a052004417c6a210402402002280204220120022802002208470d0020022802082203200228020c22064904402002200120032003200620036b41027541016a41026d41027422066a103b22013602042002200228020820066a3602080c010b200541186a200620086b2201410175410120011b2201200141036a410276200228021010392002280208210620022802042101034020012006470440200528022020012802003602002005200528022041046a360220200141046a21010c010b0b20022902002109200220052903183702002002290208210a20022005290320370208200520093703182005200a370320103a200228020421010b2001417c6a200428020036020020022002280204417c6a3602040c010b0b0b200541186a2007102a200528021c410036020041012101200041186a0b2202200228020020016a360200200541306a240020000b2d01017f230041106b220224002000410010332002200110422200101920001044410110331a200241106a24000b1400200028020004402000103120002802001a0b0b2801017f200028020820002802046b2201410874417f6a410020011b200028021420002802106a6b0ba10202057f017e230041206b22052400024020002802082202200028020c2206470d0020002802042203200028020022044b04402000200320022003200320046b41027541016a417e6d41027422046a103822023602082000200028020420046a3602040c010b200541086a200620046b2202410175410120021b220220024102762000410c6a10392103200028020821042000280204210203402002200446450440200328020820022802003602002003200328020841046a360208200241046a21020c010b0b2000290200210720002003290200370200200320073702002000290208210720002003290208370208200320073702082003103a200028020821020b200220012802003602002000200028020841046a360208200541206a24000b2501017f200120006b2201410275210320010440200220002001103e0b200220034102746a0b5f01017f2000410036020c200041106a200336020002402001044020014180808080044f0d012001410274104121040b200020043602002000200420024102746a22023602082000200420014102746a36020c2000200236020420000f0b000b3101027f200028020821012000280204210203402001200247044020002001417c6a22013602080c010b0b20002802001a0b1b00200120006b22010440200220016b220220002001103e0b20020bf80801067f0340200020046a2105200120046a220341037145200220044672450440200520032d00003a0000200441016a21040c010b0b200220046b210602402005410371220845044003402006411049450440200020046a2202200120046a2203290200370200200241086a200341086a290200370200200441106a2104200641706a21060c010b0b027f2006410871450440200120046a2103200020046a0c010b200020046a2202200120046a2201290200370200200141086a2103200241086a0b21042006410471044020042003280200360200200341046a2103200441046a21040b20064102710440200420032f00003b0000200341026a2103200441026a21040b2006410171450d01200420032d00003a000020000f0b024020064120490d002008417f6a220841024b0d00024002400240024002400240200841016b0e020102000b2005200120046a220628020022033a0000200541016a200641016a2f00003b0000200041036a2108200220046b417d6a2106034020064111490d03200420086a2202200120046a220541046a2802002207410874200341187672360200200241046a200541086a2802002203410874200741187672360200200241086a2005410c6a28020022074108742003411876723602002002410c6a200541106a2802002203410874200741187672360200200441106a2104200641706a21060c000b000b2005200120046a220628020022033a0000200541016a200641016a2d00003a0000200041026a2108200220046b417e6a2106034020064112490d03200420086a2202200120046a220541046a2802002207411074200341107672360200200241046a200541086a2802002203411074200741107672360200200241086a2005410c6a28020022074110742003411076723602002002410c6a200541106a2802002203411074200741107672360200200441106a2104200641706a21060c000b000b2005200120046a28020022033a0000200041016a21082004417f7320026a2106034020064113490d03200420086a2202200120046a220541046a2802002207411874200341087672360200200241046a200541086a2802002203411874200741087672360200200241086a2005410c6a28020022074118742003410876723602002002410c6a200541106a2802002203411874200741087672360200200441106a2104200641706a21060c000b000b200120046a41036a2103200020046a41036a21050c020b200120046a41026a2103200020046a41026a21050c010b200120046a41016a2103200020046a41016a21050b20064110710440200520032d00003a00002005200328000136000120052003290005370005200520032f000d3b000d200520032d000f3a000f200541106a2105200341106a21030b2006410871044020052003290000370000200541086a2105200341086a21030b2006410471044020052003280000360000200541046a2105200341046a21030b20064102710440200520032f00003b0000200541026a2105200341026a21030b2006410171450d00200520032d00003a00000b20000be10201027f02402001450d00200041003a0000200020016a2202417f6a41003a000020014103490d00200041003a0002200041003a00012002417d6a41003a00002002417e6a41003a000020014107490d00200041003a00032002417c6a41003a000020014109490d002000410020006b41037122036a220241003602002002200120036b417c7122036a2201417c6a410036020020034109490d002002410036020820024100360204200141786a4100360200200141746a410036020020034119490d002002410036021820024100360214200241003602102002410036020c200141706a41003602002001416c6a4100360200200141686a4100360200200141646a41003602002003200241047141187222036b2101200220036a2102034020014120490d0120024200370300200241186a4200370300200241106a4200370300200241086a4200370300200241206a2102200141606a21010c000b000b20000b8d0301037f024020002001460d00200120006b20026b410020024101746b4d0440200020012002103c1a0c010b20002001734103712103027f024020002001490440200020030d021a410021030340200120036a2104200020036a2205410371450440200220036b210241002103034020024104490d04200320056a200320046a280200360200200341046a21032002417c6a21020c000b000b20022003460d04200520042d00003a0000200341016a21030c000b000b024020030d002001417f6a21030340200020026a22044103714504402001417c6a21032000417c6a2104034020024104490d03200220046a200220036a2802003602002002417c6a21020c000b000b2002450d042004417f6a200220036a2d00003a00002002417f6a21020c000b000b2001417f6a210103402002450d03200020026a417f6a200120026a2d00003a00002002417f6a21020c000b000b200320046a2101200320056a0b210303402002450d01200320012d00003a00002002417f6a2102200341016a2103200141016a21010c000b000b0b3501017f230041106b220041908b0436020c41f80a200028020c41076a417871220036020041fc0a200036020041800b3f003602000b970101047f230041106b220124002001200036020c2000047f41800b200041086a2202411076220041800b2802006a220336020041fc0a200241fc0a28020022026a41076a417871220436020002400240200341107420044d044041800b200341016a360200200041016a21000c010b2000450d010b200040000d0010000b20022001410c6a4104103c41086a0541000b200141106a24000b0b002000410120001b10400ba10101037f20004200370200200041086a2202410036020020012d0000410171450440200020012902003702002002200141086a28020036020020000f0b20012802082103024020012802042201410a4d0440200020014101743a0000200041016a21020c010b200141106a4170712204104121022000200136020420002004410172360200200020023602080b2002200320011043200120026a41003a000020000b100020020440200020012002103c1a0b0b130020002d0000410171044020002802081a0b0ba10201047f20002001470440200128020420012d00002202410176200241017122031b2102200141016a21052001280208410a2101200520031b210520002d000022034101712204044020002802002203417e71417f6a21010b200220014d0440027f2004044020002802080c010b200041016a0b210120020440200120052002103e0b200120026a41003a000020002d00004101710440200020023602040f0b200020024101743a00000f0b027f2003410171044020002802080c010b41000b1a416f2103200141e6ffffff074d0440410b20014101742203200220022003491b220341106a4170712003410b491b21030b200310412204200520021043200020023602042000200436020820002003410172360200200220046a41003a00000b0b880101037f41dc08410136020041e0082802002100034020000440034041e40841e4082802002201417f6a2202360200200141014845044041dc084100360200200020024102746a22004184016a280200200041046a28020011010041dc08410136020041e00828020021000c010b0b41e408412036020041e008200028020022003602000c010b0b0b970101027f41dc08410136020041e008280200220145044041e00841e80836020041e80821010b024041e4082802002202412046044041840210402201450d012001418402103d220141e00828020036020041e008200136020041e4084100360200410021020b41e408200241016a360200200120024102746a22014184016a4100360200200141046a20003602000b41dc0841003602000b3801017f41ec0a420037020041f40a410036020041742100034020000440200041f80a6a4100360200200041046a21000c010b0b410410470b070041ec0a10440b750020004200370210200042ffffffff0f3702082000200129020037020002402002410871450d002000104b20012802044f0d002002410471044010000c010b200042003702000b02402002411071450d002000104b20012802044d0d0020024104710440100020000f0b200042003702000b20000b2e01017f200028020445044041000f0b4101210120002802002c0000417f4c047f2000104c2000104d6a0541010b0b5b00027f027f41002000280204450d001a410020002802002c0000417f4a0d011a20002802002d0000220041bf014d04404100200041b801490d011a200041c97e6a0c010b4100200041f801490d001a200041897e6a0b41016a0b0bff0201037f200028020445044041000f0b2000104e41012102024020002802002c00002201417f4a0d00200141ff0171220341b7014d0440200341807f6a0f0b02400240200141ff0171220141bf014d0440024020002802042201200341c97e6a22024d047f100020002802040520010b4102490d0020002802002d00010d0010000b200241054f044010000b20002802002d000145044010000b4100210241b7012101034020012003460440200241384f0d030c0405200028020020016a41ca7e6a2d00002002410874722102200141016a21010c010b000b000b200141f7014d0440200341c07e6a0f0b024020002802042201200341897e6a22024d047f100020002802040520010b4102490d0020002802002d00010d0010000b200241054f044010000b20002802002d000145044010000b4100210241f701210103402001200346044020024138490d0305200028020020016a418a7e6a2d00002002410874722102200141016a21010c010b0b0b200241ff7d490d010b10000b20020b4101017f200028020445044010000b0240200028020022012d0000418101470d00200028020441014d047f100020002802000520010b2c00014100480d0010000b0bb50102057f017e230041106b22022400200041046a210102402000280200220304402001280200220504402005200041086a2802006a21040b20002004360204200041086a2003360200200241086a20014100200420031050105120002002290308220637020420004100200028020022002006422088a76b2201200120004b1b3602000c010b200020012802002201047f2001200041086a2802006a0541000b360204200041086a41003602000b200241106a24000b3c01017f230041306b22022400200220013602142002200036021020022002290310370308200241186a200241086a4114104a104b200241306a24000b5a01027f2000027f0240200128020022054504400c010b200220036a200128020422014b2001200249720d00410020012003490d011a200220056a2104200120026b20032003417f461b0c010b41000b360204200020043602000be70101037f230041106b2204240020004200370200200041086a410036020020012802042103024002402002450440200321020c010b410021022003450d002003210220012802002d000041c001490d00200441086a2001105320004100200428020c2201200428020822022001105022032003417f461b20024520012003497222031b220536020820004100200220031b3602042000200120056b3602000c010b20012802002103200128020421012000410036020020004100200220016b20034520022001497222021b36020820004100200120036a20021b3602040b200441106a240020000b2101017f2001104d220220012802044b044010000b200020012001104c200210510bd60202077f017e230041206b220324002001280208220420024b0440200341186a2001105320012003280218200328021c105036020c200341106a20011053410021042001027f410020032802102206450d001a410020032802142208200128020c2207490d001a200820072007417f461b210520060b360210200141146a2005360200200141003602080b200141106a210903400240200420024f0d002001280214450d00200341106a2001105341002104027f410020032802102207450d001a410020032802142208200128020c2206490d001a200820066b2104200620076a0b21052001200436021420012005360210200341106a20094100200520041050105120012003290310220a3702102001200128020c200a422088a76a36020c2001200128020841016a22043602080c010b0b20032009290200220a3703082003200a370300200020034114104a1a200341206a24000b980101037f200028020445044041000f0b2000104e200028020022022c0000220141004e044020014100470f0b027f4101200141807f460d001a200141ff0171220341b7014d0440200028020441014d047f100020002802000520020b2d00014100470f0b4100200341bf014b0d001a2000280204200141ff017141ca7e6a22014d047f100020002802000520020b20016a2d00004100470b0b800101047f230041106b2201240002402000280204450d0020002802002d000041c001490d00200141086a20001053200128020c210003402000450d01200141002001280208220320032000105022046a20034520002004497222031b3602084100200020046b20031b2100200241016a21020c000b000b200141106a240020020b2d00200020021058200028020020002802046a20012002103c1a2000200028020420026a3602042000200310590b1b00200028020420016a220120002802084b044020002001105b0b0b820201047f02402001450d00034020002802102202200028020c460d01200241786a28020020014904401000200028021021020b200241786a2203200328020020016b220136020020010d012000200336021020004101200028020422042002417c6a28020022016b22021028220341016a20024138491b220520046a105a200120002802006a220420056a20042002103e0240200241374d0440200028020020016a200241406a3a00000c010b200341f7016a220441ff014d0440200028020020016a20043a00002000280200200120036a6a210103402002450d02200120023a0000200241087621022001417f6a21010c000b000b10000b410121010c000b000b0b0f0020002001105b200020013602040b2f01017f200028020820014904402001104020002802002000280204103c210220002001360208200020023602000b0b8d0201057f02402001044020002802042104200041106a2802002202200041146a280200220349044020022001ad2004ad422086843702002000200028021041086a3602100f0b027f41002002200028020c22026b410375220541016a2206200320026b2202410275220320032006491b41ffffffff01200241037541ffffffff00491b2202450d001a200241037410410b2103200320054103746a22052001ad2004ad4220868437020020052000280210200028020c22016b22046b2106200441014e0440200620012004103c1a200028020c21010b2000200320024103746a3602142000200541086a3602102000200636020c2001450d010f0b200041c001105d200041004100410110570b0b2500200041011058200028020020002802046a20013a00002000200028020441016a3602040b5e01027f20011028220241b7016a22034180024e044010000b2000200341ff0171105d2000200028020420026a105a200028020420002802006a417f6a2100034020010440200020013a0000200141087621012000417f6a21000c010b0b0b7701027f2001280200210341012102024002400240024020012802042201410146044020032c000022014100480d012000200141ff0171105d0c040b200141374b0d01200121020b200020024180017341ff0171105d0c010b20002001105e200121020b200020032002410010570b20004101105920000bba0202037f037e024020015004402000418001105d0c010b20014280015a044020012105034020052006845045044020064238862005420888842105200241016a2102200642088821060c010b0b0240200241384f04402002210303402003044020034108762103200441016a21040c010b0b200441c9004f044010000b2000200441b77f6a41ff0171105d2000200028020420046a105a200028020420002802006a417f6a21042002210303402003450d02200420033a0000200341087621032004417f6a21040c000b000b200020024180017341ff0171105d0b2000200028020420026a105a200028020420002802006a417f6a210203402001200784500d02200220013c0000200742388620014208888421012002417f6a2102200742088821070c000b000b20002001a741ff0171105d0b2000410110590b0b5601004180080b4f696e6974006164645f6d795f6d657373616765006765745f6d795f6d6573736167655f73697a65006765745f6d795f6d6573736167655f68656164006765745f6d795f6d6573736167655f626f6479";

    public static String BINARY = BINARY_0;

    public static final String FUNC_ADD_MY_MESSAGE = "add_my_message";

    public static final String FUNC_GET_MY_MESSAGE_SIZE = "get_my_message_size";

    public static final String FUNC_GET_MY_MESSAGE_HEAD = "get_my_message_head";

    public static final String FUNC_GET_MY_MESSAGE_BODY = "get_my_message_body";

    protected OneInherit(String contractAddress, Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected OneInherit(String contractAddress, Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<OneInherit> deploy(Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(OneInherit.class, web3j, credentials, contractGasProvider, encodedConstructor);
    }

    public static RemoteCall<OneInherit> deploy(Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(OneInherit.class, web3j, transactionManager, contractGasProvider, encodedConstructor);
    }

    public static RemoteCall<OneInherit> deploy(Web3j web3j, Credentials credentials, GasProvider contractGasProvider, BigInteger initialVonValue) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(OneInherit.class, web3j, credentials, contractGasProvider, encodedConstructor, initialVonValue);
    }

    public static RemoteCall<OneInherit> deploy(Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider, BigInteger initialVonValue) {
        String encodedConstructor = WasmFunctionEncoder.encodeConstructor(BINARY, Arrays.asList());
        return deployRemoteCall(OneInherit.class, web3j, transactionManager, contractGasProvider, encodedConstructor, initialVonValue);
    }

    public RemoteCall<TransactionReceipt> add_my_message(My_message one_message) {
        final WasmFunction function = new WasmFunction(FUNC_ADD_MY_MESSAGE, Arrays.asList(one_message), Void.class);
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> add_my_message(My_message one_message, BigInteger vonValue) {
        final WasmFunction function = new WasmFunction(FUNC_ADD_MY_MESSAGE, Arrays.asList(one_message), Void.class);
        return executeRemoteCallTransaction(function, vonValue);
    }

    public RemoteCall<Uint8> get_my_message_size() {
        final WasmFunction function = new WasmFunction(FUNC_GET_MY_MESSAGE_SIZE, Arrays.asList(), Uint8.class);
        return executeRemoteCall(function, Uint8.class);
    }

    public RemoteCall<String> get_my_message_head(Uint8 index) {
        final WasmFunction function = new WasmFunction(FUNC_GET_MY_MESSAGE_HEAD, Arrays.asList(index), String.class);
        return executeRemoteCall(function, String.class);
    }

    public RemoteCall<String> get_my_message_body(Uint8 index) {
        final WasmFunction function = new WasmFunction(FUNC_GET_MY_MESSAGE_BODY, Arrays.asList(index), String.class);
        return executeRemoteCall(function, String.class);
    }

    public static OneInherit load(String contractAddress, Web3j web3j, Credentials credentials, GasProvider contractGasProvider) {
        return new OneInherit(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static OneInherit load(String contractAddress, Web3j web3j, TransactionManager transactionManager, GasProvider contractGasProvider) {
        return new OneInherit(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class Message {
        public String head;
    }

    public static class My_message {
        public Message baseClass;

        public String body;

        public String end;
    }
}
