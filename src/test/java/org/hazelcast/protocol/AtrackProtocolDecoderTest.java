package org.hazelcast.protocol;

import org.junit.Test;
import org.hazelcast.ProtocolTest;

public class AtrackProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        AtrackProtocolDecoder decoder = new AtrackProtocolDecoder(null);

        verifyPositions(decoder, binary(
                "03012c433538312c3135372c342c3335383838373039353933353839342c32303230303430313037353933312c32303230303430313037353933312c32303230303430313037353933312c32373933393534312c2d32363132313934332c3238382c302c3136322c31312c302c302c302c302c2c323030302c323030302c1a2c313537352c302c302c302c3132342c302c31302c302c302c302c302c3132352c302c372c302c0d0a"));

        verifyPositions(decoder, buffer(
                "@P,93D1,419,0,357766091026083,1557178589,1557178590,1557178590,-121899637,37406241,338,230,2809,8,0,0,0,0,,2000,2000,\r\n"));

        verifyPositions(decoder, buffer(
                "@P,3A34,146,41431,353816057242284,20180622015809,20180622015809,20180622015809,9720689,4014230,61,2,0,20,1,0,0,0,0,2000,2000,12160,42,624,002,20009,20014,\r\n"));

        verifyPositions(decoder, buffer(
                "@P,1126,121,104547,358901048091554,20180412143513,20180412143514,20180413060000,16423389,48178700,108,2,6.5,9,0,0,0,0,0,2000,2000,\r\n"));

        verifyPositions(decoder, buffer(
                "@P,434E,124,104655,358901048091554,20180412143706,20180412143706,20180413060107,16423389,48178700,108,121,6.5,10,0,0,0,0,0,2000,2000,\r\n"));

        verifyPositions(decoder, binary(
                "4050b5ed004a2523000310c83713f8c05a88b43e5a88b43f5a88b43f021e0ad5fffdc0a800f3020003059100080000000000000007d007d046554c533a463d3230393120743d3137204e3d3039303100"));

        verifyAttributes(decoder, buffer(
                "$INFO=358683066267395,AX7,Rev.0.61 Build.1624,358683066267395,466924131626767,89886920041316267670,144,0,9,1,12,1,0\r\n"));

        decoder.setLongDate(true);

        verifyPositions(decoder, binary(
                "0203b494003c00eb00014104d8dd3a3e07de011b0b1f0307de011b0b1f0307de011b0b1f0300307f28030574d30000020000000600160100020000000007d007d000"));

        decoder.setLongDate(false);

        decoder.setCustom(true);

        verifyPositions(decoder, buffer(
                "@P,9493,402,143,356961075931165,1546830150,1546830151,1546830151,-88429209,44271154,54,10,0,10,1,0,0,0,1858AE010000,2000,2000,\u001A,%CI%FL%ML%VN%PD%FC%EL%ET%AT%MF%MV%BV%DT%GN%GV%ME%RL%RP%SA%SM%TR%IA%MP,0,0,2T1KR32E28C706185,0,1,0,7,251,89,118,41,0,00A5001A040800A5001A040B00A5001A040C00A5001A040900A4001C040D00A50019040900A60019040900A4001B040B00A5001A040900A7001A040E\u001A,008CFE7C03C4\u001A,356961075931165,0,0,12,0,18,5,0\n"));

        verifyPositions(decoder, buffer(
                "@P,FD34,720,12256,357520076794151,1535445349,1535445354,1535500603,106784149,-6283086,105,2,138,0,3,0,0,0,,2000,2000,,%CI%TR%MV%BV%AT%SA%ET%GQ%GS%PC%RP%OD%AV1%XS%VS,0,0,0,0,0,0,0,0,1011677,0,138,0,0,0\r\n",
                "1535445376,1535445374,1535500603,106783763,-6282981,105,101,138,6,2,0,0,0,,2000,2000,,%CI%TR%MV%BV%AT%SA%ET%GQ%GS%PC%RP%OD%AV1%XS%VS,0,141,41,60,12,0,0,7,1011677,0,138,0,0,0\r\n",
                "1535445380,1535445378,1535500603,106783763,-6282981,105,103,138,6,2,0,0,0,,2000,2000,,%CI%TR%MV%BV%AT%SA%ET%GQ%GS%PC%RP%OD%AV1%XS%VS,0,135,41,61,12,0,0,9,1011677,0,138,0,0,0\r\n",
                "1535445415,1535445415,1535500603,106783763,-6282981,105,2,138,7,2,0,0,0,,2000,2000,,%CI%TR%MV%BV%AT%SA%ET%GQ%GS%PC%RP%OD%AV1%XS%VS,0,135,41,61,12,0,21,10,1011677,0,138,0,0,0\r\n"));

        verifyPositions(decoder, buffer(
                "@P,0EBB,687,1917,357298070426498,1534718280,1534718279,1534739774,-88643875,44210148,270,2,57128,6,1,130,0,0,,2000,2000,,%CI%GQ%GS%CN%CE%MV%SA,5,10,310260,7888593,137,16\r\n",
                "1534718283,1534718283,1534739774,-88645243,44210145,269,2,57129,6,1,129,0,0,,2000,2000,,%CI%GQ%GS%CN%CE%MV%SA,5,10,310260,7888593,137,16\r\n",
                "1534718286,1534718285,1534739774,-88646581,44210136,269,2,57130,6,1,127,0,0,,2000,2000,,%CI%GQ%GS%CN%CE%MV%SA,99,10,0,0,137,16\r\n",
                "1534718289,1534718288,1534739774,-88647911,44210123,269,2,57131,6,1,127,0,0,,2000,2000,,%CI%GQ%GS%CN%CE%MV%SA,99,10,0,0,137,16\r\n",
                "1534718292,1534718291,1534739774,-88649229,44210111,269,2,57132,6,1,124,0,0,,2000,2000,,%CI%GQ%GS%CN%CE%MV%SA,99,10,0,0,136,16\r\n"));

        verifyPositions(decoder, binary(
                "4050b63b02c401af000144a77a21281d5b79d8ef5b79d8ef5b79d8f0fab84831029f35580056020003144d00080100130000000007d007d00025434925434525434e25475125475325464c254d4c25564e25504425464325454c254554254344254154254d46254d5625425625434d25445425474c25474e254756254c43254d4525524c25525025534125534d255452254941254d5000000000000004bbf41f0900003254314b5233324532384337303631383500000000000053005f3839303132363038383132313532343737353900000000fd078e0085002900011a2e3da1882700687474703a2f2f6d6170732e676f6f676c652e636f6d2f6d6170733f713d34332e3938383331322c2d38382e35383631383920000013ff4e04190023ff13041100a3ffdc0402009affde03fc00a4ffe3040c0093ffab03ee004dffbb04130012ff7a04180010ff6e04100037ff4d0402ffd8000c04140000000144a77a21281d0009470c00131b2b005b79d8f05b79d8f05b79d8f0fab8488e029f356f0043020003144d00080100170000000007d007d00025434925434525434e25475125475325464c254d4c25564e25504425464325454c254554254344254154254d46254d5625425625434d25445425474c25474e254756254c43254d4525524c25525025534125534d255452254941254d5000000000000004bbf41f0900003254314b5233324532384337303631383500000000000052005f3839303132363038383132313532343737353900000000fd09190085002900011a2e3da1882700687474703a2f2f6d6170732e676f6f676c652e636f6d2f6d6170733f713d34332e3938383333352c2d38382e35383630393820000013ff4e04190023ff1304110017ff0c0424000cff30041a00a4ffe3040c0093ffab03ee004dffbb04130012ff7a04180010ff6e04100037ff4d0402ffd8000c04140000000144a77a21281d0009470c00171c2b00"));

        verifyPositions(decoder, binary(
                "405ad77c01670410000144a77a21281d5b74d2335b74d2335b74d233fabaf3bc02a38d3d010c0200030f8e000701001a0000000007d007d00025434925434525434e25475125475325464c254d4c25564e25504425464325454c254554254344254154254d46254d5625425625434d25445425474c25474e254756254c43254d4525524c25525025534125534d255452254941254d5000000000000004bbf41c0900003254314b523332453238433730363138350000000000004800543839303132363038383132313532343737353900000000ec06a50089002900011a2e3da1882700687474703a2f2f6d6170732e676f6f676c652e636f6d2f6d6170733f713d34342e3237323935372c2d38382e34313132303120000075ff4903fb006fff63040a004dff5d04080030ffa10407003b001304060026000503f9001e002504020078ff6204000073ff7d03f9007aff6903f3ffc0001804040000000144a77a21281d00073f0c001a182400"));

        verifyPositions(decoder, buffer(
                "@P,6254,235,989,356961075931165,1534381563,1534381564,1534381564,-88429188,44271225,70,2,200563,8,1,0,0,0,,2000,2000,,%CI%CE%CN%GQ%GS%FL%ML%VN%PD%FC%EL%ET%CD%AT%MF%MV,0,310260,18,9,0,0,2T1KR32E28C706185,0,0,0,54,8901260881215247759,252,489,123"));

        verifyPositions(decoder, buffer(
                "@P,27A6,663,707,356961075931165,1534211298,1534211297,1534211437,-88429190,44271135,288,2,200235,8,1,0,0,0,,2000,2000,,%CI%CE%CN%GQ%GS%FL%ML%VN%PD%FC%EL%ET%CD%AT%MF%MV,0,310260,17,9,0,0,2T1KR32E28C706185,0,0,0,80,8901260881215247759,251,59,124\r\n",
                "1534211353,1534211357,1534211437,-88429190,44271135,288,2,200235,7,1,0,0,0,,2000,2000,,%CI%CE%CN%GQ%GS%FL%ML%VN%PD%FC%EL%ET%CD%AT%MF%MV,0,310260,17,2,0,0,2T1KR32E28C706185,0,0,0,79,8901260881215247759,251,60,124\r\n",
                "1534211417,1534211417,1534211437,-88429190,44271135,288,2,200235,7,1,0,0,0,,2000,2000,,%CI%CE%CN%GQ%GS%FL%ML%VN%PD%FC%EL%ET%CD%AT%MF%MV,0,310260,17,2,0,0,2T1KR32E28C706185,0,0,0,78,8901260881215247759,251,56,124\r\n"));

        verifyPositions(decoder, buffer(
                "@P,CA4B,122,1,358683064932578,1533633976,1533633975,1533633975,121562641,25082649,72,0,1638,15,0,0,1,0,,2000,2000,,%CI%MV%BV,143,0\r\n"));

        verifyPositions(decoder, binary(
                "405025e30096eb730001452efaf6a7d6562fe4f8562fe4f7562fe52c02a006d902273f810064650000e0f5000a0100000000000007d007d000254349255341254d5625425625475125415400090083002a1a000001a8562fe4f8562fe4f7562fe52c02a006d902273f810064020000e0f5000a0100000000000007d007d000254349255341254d5625425625475125415400090083002a1a000001a8"));

        decoder.setCustom(false);

        verifyNull(decoder, binary(
                "fe0200014104d8f196820001"));

        verifyPositions(decoder, binary(
                "40503835003300070001441c3d8ed1c400000000000000c9000000c900000000000000000000020000000003de0100000000000007d007d000"),
                position("1970-01-01 00:00:00.000", true, 0.00000, 0.00000));

        verifyPositions(decoder, binary(
                "4050993f005c000200014104d8f19682525666c252568c3c52568c63ffc8338402698885000002000009cf03de0100000000000007d007d000525666c252568c5a52568c63ffc8338402698885000002000009cf03de0100000000000007d007d000"));

        verifyPositions(decoder, binary(
                "40501e58003301e000014104d8f19682525ecd5d525ee344525ee35effc88815026ab4d70000020000104403de01000b0000000007d007d000"));

        verifyPositions(decoder, binary(
                "40501e58003301e000014104d8f19682525ecd5d525ee344525ee35effc88815026ab4d70000020000104403de01000b0000000007d007d000000000000000"));

        verifyAttributes(decoder, buffer(
                "$OK\r\n"));

        verifyAttributes(decoder, buffer(
                "$ERROR=101\r\n"));

    }

    /*@Test
    public void testDecodeCustom() throws Exception {

        AtrackProtocolDecoder decoder = new AtrackProtocolDecoder(null);

        decoder.setCustom(true);

        decoder.setForm("%AT%BV%MV%SA%VN%PD%IA%MP%EL%ET%FC%FL%RP%ML%MF%TR%EH%DL%EG%HA%HB%HC%IP%MT");

        verifyPositions(decoder, buffer(
                "@P,7E02,186,0,357766091026083,1558908265,1558908266,1558908266,-121900220,37407524,175,2,6,6,1,39,0,0,,2000,2000, ,3,40,142,12,JN8AZ1MU1BW066090,0,30,0,58,90,22,72,1187,0,1232,9,409,0,1,0,0,0,0,1\r\n"));

        decoder.setForm("%AT%BV%CD%CE%CM%CN%DT%GN%GQ%GS%GV%LC%ME%MV%RL%SA%SM%CS%HT%VN%PD%IA%MP%EL%ET%FC%FL%RP%ML%MF%TR%EH%CR%DL%EG%HA%HB%HC%IP%MT%PF");

        verifyPositions(decoder, buffer(
                "@P,DCCE,422,5818,357766091026083,1557904779,1557904780,1557904780,-121899644,37406291,129,2,21,10,0,0,0,0,,2000,2000,,13,40,8942310017000752067,21096194,295050910083206,310260,0,FF00001F0393FF01001E0395FF01001E0394FF01001F0393FF02001D0393FF00001F0394FF0100200394FF01001F0393FF02001F0395FF0100200394,20,10,002C005C03B4,14953,357766091026083,125,38,11,0,1,Device:Fail,JN8AZ1MU1BW066090,0,0,0,0,0,99,0,0,0,0,0,264,5,0,0,0,0,0,0,0,0\r\n"));

    }*/

}