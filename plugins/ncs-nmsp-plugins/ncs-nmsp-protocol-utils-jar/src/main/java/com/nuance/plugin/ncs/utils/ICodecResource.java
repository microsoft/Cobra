//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.nuance.plugin.ncs.utils;

import com.google.common.primitives.Shorts;
import com.nuance.cobra.utils.audio.AudioCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public interface ICodecResource {
    Logger logger = LogManager.getLogger();
    enum Codec {
        UNKNOWN(0, ICodecResource.Rate.UNKNOWN),
        PCM16(2, ICodecResource.Rate.EIGHT, Type.L16, AudioCodec.PCM_16_8K.getWebapiString()),
        PCM16_8K(2, Rate.BASIC, Type.L16, AudioCodec.PCM_16_8K.getWebapiString()),
        PCM16_11K(4, ICodecResource.Rate.ELEVEN, Type.L16),
        PCM16_16K(5, ICodecResource.Rate.SIXTEEN, Type.L16, AudioCodec.PCM_16_16K.getWebapiString()),
        PCM16_22K(7, ICodecResource.Rate.TWENTY_TWO, Type.L16, AudioCodec.PCM_16_22K.getWebapiString()),
        PCM16_22050(7, Rate.TWENTY_TWO_FIFTY, Type.L16, AudioCodec.PCM_16_22K.getWebapiString()),
        PCM16_24K(1112, Rate.TWENTY_FOUR, Type.L16, AudioCodec.PCM_16_22K.getWebapiString()),
        PCM16_32K(6, ICodecResource.Rate.THIRTY_TWO, Type.L16),
        IMA_ADPCM_16K(1280, ICodecResource.Rate.SIXTEEN, Type.IMA_ADPCM, AudioCodec.IMA_ADPCM.getWebapiString()), // This needs to appear above 777 for LGI. They expect ami_adpcm to show up as 1280 in logs
        IMA_ADPCM(777, ICodecResource.Rate.SIXTEEN, Type.IMA_ADPCM, AudioCodec.IMA_ADPCM.getWebapiString()),
        ULAW(8, ICodecResource.Rate.BASIC),
        ALAW(9, ICodecResource.Rate.BASIC),
        DSR_ES201108(48, ICodecResource.Rate.EIGHT),
        DSR_ES201108_11K(49, ICodecResource.Rate.ELEVEN),
        AMR_R0(80, ICodecResource.Rate.EIGHT),
        AMR_R1(81, ICodecResource.Rate.EIGHT),
        AMR_R2(82, ICodecResource.Rate.EIGHT),
        AMR_R3(83, ICodecResource.Rate.EIGHT),
        AMR_R4(84, ICodecResource.Rate.EIGHT),
        AMR_R5(85, ICodecResource.Rate.EIGHT),
        AMR_R6(86, ICodecResource.Rate.EIGHT),
        AMR_R7(87, ICodecResource.Rate.EIGHT),
        AMR_NBO_R0(128, ICodecResource.Rate.EIGHT),
        AMR_NBO_R1(129, ICodecResource.Rate.EIGHT),
        AMR_NBO_R2(130, ICodecResource.Rate.EIGHT),
        AMR_NBO_R3(131, ICodecResource.Rate.EIGHT),
        AMR_NBO_R4(132, ICodecResource.Rate.EIGHT),
        AMR_NBO_R5(133, ICodecResource.Rate.EIGHT),
        AMR_NBO_R6(134, ICodecResource.Rate.EIGHT),
        AMR_NBO_R7(135, ICodecResource.Rate.EIGHT),
        QSC_RF(98, ICodecResource.Rate.EIGHT),
        QSC_RV(99, ICodecResource.Rate.EIGHT),
        EVRC(112, ICodecResource.Rate.EIGHT),
        SPEEX_NB_NR0(256, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_NR1(257, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_NR2(258, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_NR3(259, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_NR4(260, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_NR5(261, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_NR6(262, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_NR7(263, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_SB_NR0(352, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR1(353, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR2(354, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR3(355, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR4(356, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR5(357, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR6(358, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR7(359, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR8(360, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR9(361, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_NR10(362, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_NR0(264, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR1(265, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR2(272, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR3(273, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR4(274, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR5(275, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR6(276, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR7(277, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR8(278, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR9(279, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_NR10(280, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_UWB_NR0(281, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR1(288, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR2(289, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR3(290, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR4(291, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR5(292, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR6(293, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR7(294, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR8(295, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR9(296, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_NR10(297, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR0(304, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR1(305, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR2(306, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR3(307, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR4(308, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR5(309, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR6(310, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_NR7(311, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR0(368, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR1(369, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR2(370, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR3(371, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR4(372, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR5(373, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR6(374, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR7(375, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR8(376, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR9(377, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_NR10(378, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR0(312, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR1(313, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR2(320, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR3(321, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR4(322, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR5(323, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR6(324, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR7(325, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR8(326, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR9(327, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_NR10(328, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR0(329, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR1(336, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR2(337, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR3(338, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR4(339, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR5(340, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR6(341, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR7(342, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR8(343, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR9(344, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_NR10(345, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_NB_R0(512, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R1(513, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R2(514, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R3(515, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R4(516, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R5(517, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R6(518, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R7(519, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R8(520, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R9(521, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_R10(522, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_NB_NR6.getWebapiString()),
        SPEEX_NB_VBR_R0(528, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R1(529, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R2(530, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R3(531, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R4(532, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R5(533, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R6(534, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R7(535, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R8(536, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R9(537, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_NB_VBR_R10(538, ICodecResource.Rate.EIGHT, ICodecResource.Type.SPEEX),
        SPEEX_SB_R0(800, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R1(801, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R2(802, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R3(803, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R4(804, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R5(805, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R6(806, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R7(807, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R8(808, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R9(809, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_R10(810, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R0(816, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R1(817, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R2(818, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R3(819, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R4(820, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R5(821, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R6(822, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R7(823, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R8(824, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R9(825, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_SB_VBR_R10(826, ICodecResource.Rate.ELEVEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_R0(544, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R1(545, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R2(546, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R3(547, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R4(548, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R5(549, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R6(550, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R7(551, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R8(552, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R9(553, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_R10(554, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX, AudioCodec.SPEEX_WB_NR8.getWebapiString()),
        SPEEX_WB_VBR_R0(560, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R1(561, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R2(562, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R3(563, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R4(564, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R5(565, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R6(566, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R7(567, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R8(568, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R9(569, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_WB_VBR_R10(570, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R0(576, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R1(577, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R2(578, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R3(579, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R4(580, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R5(581, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R6(582, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R7(583, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R8(584, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R9(585, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_R10(586, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R0(592, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R1(593, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R2(594, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R3(595, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R4(596, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R5(597, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R6(598, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R7(599, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R8(600, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R9(601, ICodecResource.Rate.THIRTY_TWO, ICodecResource.Type.SPEEX),
        SPEEX_UWB_VBR_R10(602, ICodecResource.Rate.THIRTY_TWO),
        MP3(608, ICodecResource.Rate.TWENTY_TWO, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_8KBPS(609, ICodecResource.Rate.EIGHT, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_16KBPS(610, ICodecResource.Rate.SIXTEEN, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_24KBPS(611, ICodecResource.Rate.TWENTY_FOUR, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_32KBPS(612, ICodecResource.Rate.THIRTY_TWO, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_40KBPS(613, ICodecResource.Rate.FORTY, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_48KBPS(614, ICodecResource.Rate.FORTY_EIGHT, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_56KBPS(615, ICodecResource.Rate.FIFTY_SIX, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_64KBPS(616, ICodecResource.Rate.SIXTY_FOUR, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_80KBPS(617, ICodecResource.Rate.EIGHTY, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_96KBPS(618, ICodecResource.Rate.NINETY_SIX, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_112KBPS(619, ICodecResource.Rate.ONE_HUNDRED_TWELVE, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_128KBPS(620, ICodecResource.Rate.ONE_HUNDRED_TWENTY_EIGHT, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_144KBPS(621, ICodecResource.Rate.ONE_HUNDRED_FORTY_FOUR, AudioCodec.PCM_16_22K.getWebapiString()),
        MP3_160KBPS(622, ICodecResource.Rate.ONE_HUNDRED_SIXTY, AudioCodec.PCM_16_22K.getWebapiString()),
        G711WBE_ALAW_R1(624, ICodecResource.Rate.BASIC),
        G711WBE_ALAW_R2A(625, ICodecResource.Rate.BASIC),
        G711WBE_ALAW_R2B(626, ICodecResource.Rate.BASIC),
        G711WBE_ALAW_R3(627, ICodecResource.Rate.BASIC),
        G711WBE_ULAW_R1(640, ICodecResource.Rate.BASIC),
        G711WBE_ULAW_R2A(641, ICodecResource.Rate.BASIC),
        G711WBE_ULAW_R2B(642, ICodecResource.Rate.BASIC),
        G711WBE_ULAW_R3(643, ICodecResource.Rate.BASIC),
        MS_GSM(656, ICodecResource.Rate.EIGHT),
        GSM(657, ICodecResource.Rate.EIGHT),
        OPUS_8K_MONO_CBR_CPLX0(1024, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX1(1025, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX2(1026, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX3(1027, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX4(1028, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX5(1029, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX6(1030, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX7(1031, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX8(1032, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX9(1033, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_CBR_CPLX10(1034, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX0(1035, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX1(1036, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX2(1037, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX3(1038, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX4(1039, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX5(1040, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX6(1041, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX7(1042, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX8(1043, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX9(1044, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_MONO_VBR_CPLX10(1045, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX0(1046, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX1(1047, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX2(1048, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX3(1049, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX4(1050, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX5(1051, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX6(1052, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX7(1053, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX8(1054, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX9(1055, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_CBR_CPLX10(1056, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX0(1057, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX1(1058, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX2(1059, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX3(1060, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX4(1061, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX5(1062, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX6(1063, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX7(1064, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX8(1065, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX9(1066, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_8K_STEREO_VBR_CPLX10(1067, ICodecResource.Rate.EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX0(1068, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX1(1069, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX2(1070, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX3(1071, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX4(1072, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX5(1073, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX6(1074, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX7(1075, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX8(1076, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX9(1077, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_CBR_CPLX10(1078, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX0(1079, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX1(1080, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX2(1081, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX3(1082, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX4(1083, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX5(1084, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX6(1085, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX7(1086, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX8(1087, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX9(1088, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_MONO_VBR_CPLX10(1089, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX0(1090, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX1(1091, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX2(1092, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX3(1093, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX4(1094, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX5(1095, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX6(1096, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX7(1097, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX8(1098, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX9(1099, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_CBR_CPLX10(1100, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX0(1101, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX1(1102, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX2(1103, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX3(1104, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX4(1105, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX5(1106, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX6(1107, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX7(1108, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX8(1109, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX9(1110, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_12K_STEREO_VBR_CPLX10(1111, ICodecResource.Rate.TWELVE, ICodecResource.Type.OPUS, AudioCodec.OPUS_8K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX0(1112, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX1(1113, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX2(1114, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX3(1115, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX4(1116, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX5(1117, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX6(1118, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX7(1119, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX8(1120, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX9(1121, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_CBR_CPLX10(1122, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX0(1123, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX1(1124, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX2(1125, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX3(1126, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX4(1127, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX5(1128, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX6(1129, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX7(1130, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX8(1131, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX9(1132, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_MONO_VBR_CPLX10(1133, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX0(1134, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX1(1135, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX2(1136, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX3(1137, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX4(1138, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX5(1139, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX6(1140, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX7(1141, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX8(1142, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX9(1143, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_CBR_CPLX10(1144, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX0(1145, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX1(1146, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX2(1147, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX3(1148, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX4(1149, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX5(1150, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX6(1151, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX7(1152, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX8(1153, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX9(1154, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_16K_STEREO_VBR_CPLX10(1155, ICodecResource.Rate.SIXTEEN, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX0(1156, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX1(1157, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX2(1158, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX3(1159, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX4(1160, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX5(1161, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX6(1162, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX7(1163, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX8(1164, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX9(1165, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_CBR_CPLX10(1166, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX0(1167, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX1(1168, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX2(1169, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX3(1170, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX4(1171, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX5(1172, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX6(1173, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX7(1174, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX8(1175, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX9(1176, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_MONO_VBR_CPLX10(1177, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX0(1178, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX1(1179, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX2(1180, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX3(1181, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX4(1182, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX5(1183, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX6(1184, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX7(1185, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX8(1186, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX9(1187, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_CBR_CPLX10(1188, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX0(1189, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX1(1190, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX2(1191, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX3(1192, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX4(1193, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX5(1194, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX6(1195, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX7(1196, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX8(1197, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX9(1198, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_24K_STEREO_VBR_CPLX10(1199, ICodecResource.Rate.TWENTY_FOUR, ICodecResource.Type.OPUS, AudioCodec.OPUS_24K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX0(1200, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX1(1201, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX2(1202, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX3(1203, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX4(1204, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX5(1205, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX6(1206, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX7(1207, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX8(1208, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX9(1209, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_CBR_CPLX10(1210, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX0(1211, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX1(1212, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX2(1213, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX3(1214, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX4(1215, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX5(1216, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX6(1217, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX7(1218, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX8(1219, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX9(1220, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_MONO_VBR_CPLX10(1221, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX0(1222, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX1(1223, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX2(1224, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX3(1225, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX4(1226, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX5(1227, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX6(1228, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX7(1229, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX8(1230, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX9(1231, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_CBR_CPLX10(1232, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX0(1233, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX1(1234, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX2(1235, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX3(1236, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX4(1237, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX5(1238, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX6(1239, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX7(1240, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX8(1241, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX9(1242, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString()),
        OPUS_48K_STEREO_VBR_CPLX10(1243, ICodecResource.Rate.FORTY_EIGHT, ICodecResource.Type.OPUS, AudioCodec.OPUS_16K.getWebapiString())
        ;

        private static final ICodecResource.Codec[] CODEC_LOOKUP_TABLE = makeCodecLookupTable();
        private final short nmaidCode;
        private final ICodecResource.Rate rate;
        private final ICodecResource.Type type;
        private final String webApiName;

        Codec(int nmaidCode, ICodecResource.Rate rate, ICodecResource.Type type, String webAPIName) {
            this.nmaidCode = Shorts.checkedCast(nmaidCode);
            this.rate = rate;
            this.type = type;
            this.webApiName = webAPIName;
        }

        Codec(int nmaidCode, ICodecResource.Rate rate, ICodecResource.Type type) {
            this(nmaidCode, rate, type, null);
        }

        Codec(int nmaidCode, ICodecResource.Rate rate, String webAPIName) {
            this(nmaidCode, rate, ICodecResource.Type.OTHER, webAPIName);
        }

        Codec(int nmaidCode, ICodecResource.Rate rate) {
            this(nmaidCode, rate, ICodecResource.Type.OTHER);
        }

        private static ICodecResource.Codec[] makeCodecLookupTable() {
            ICodecResource.Codec[] values = values();
            int maxCodecValue = 0;
            ICodecResource.Codec[] lookupTable = values;

            for (int i = 0; i < values.length; ++i) {
                ICodecResource.Codec codec = lookupTable[i];
                maxCodecValue = Math.max(maxCodecValue, codec.getNmaidCode());
            }

            lookupTable = new ICodecResource.Codec[maxCodecValue + 1];
            Arrays.fill(lookupTable, UNKNOWN);

            for (Codec codec : values) {
                lookupTable[codec.getNmaidCode()] = codec;
            }

            return lookupTable;
        }

        public static ICodecResource.Codec fromNMAINCodecId(int codec) {
            ICodecResource.Codec codecEnum;
            if (codec >= 0 && codec < CODEC_LOOKUP_TABLE.length) {
                codecEnum = CODEC_LOOKUP_TABLE[codec];
            } else {
                codecEnum = UNKNOWN;
            }

            return codecEnum;
        }

        public static Codec fromWebApiString(String cobraName) {
            for (Codec codec : Codec.values()) {
                if (codec.webApiName != null && codec.webApiName.equalsIgnoreCase(cobraName)) {
                    return codec;
                }
            }
            return UNKNOWN;
        }

        public static ICodecResource.Codec fromMimeTypeString(String codec) {
            String type = codec.split(";")[0].toLowerCase();
            String parameters = codec.substring(type.length()).toLowerCase();
            if (!type.split("/")[0].equalsIgnoreCase("audio"))
            {
                logger.error("Failed to parse codec string {}", codec);
                return UNKNOWN;
            }

            Type codecType = Type.fromString(type.split("/")[1]);
            Rate codecRate = Rate.fromString(parameters);

            for (Codec toMatch: Codec.values()) {
                if (toMatch.type == codecType && toMatch.rate == codecRate) {
                    return toMatch;
                }
            }

            logger.error("Failed to parse codec string {}", codec);
            return UNKNOWN;
        }

        public short getNmaidCode() {
            return this.nmaidCode;
        }

        public ICodecResource.Rate getRate() {
            return this.rate;
        }

        public ICodecResource.Type getType() {
            return this.type;
        }

        public String getWebAPIString() throws IOException {
            if (webApiName != null) {
                return webApiName;
            }
            throw new IOException("Codec not supported by webapi. " + this.name());
        }

        public String getMimeType() {
            return "audio/" + this.type.getName() + ";rate=" + this.rate.getRate();
        }
    }

    enum Rate {
        UNKNOWN(0),
        BASIC(8000),
        EIGHT(8000),
        ELEVEN(11000),
        TWELVE(12000),
        SIXTEEN(16000),
        TWENTY_TWO(22000),
        TWENTY_TWO_FIFTY(22050),
        TWENTY_FOUR(24000),
        THIRTY_TWO(32000),
        FORTY(40000),
        FORTY_EIGHT(48000),
        FIFTY_SIX(56000),
        SIXTY_FOUR(64000),
        EIGHTY(80000),
        NINETY_SIX(96000),
        ONE_HUNDRED_TWELVE(112000),
        ONE_HUNDRED_TWENTY_EIGHT(128000),
        ONE_HUNDRED_FORTY_FOUR(144000),
        ONE_HUNDRED_SIXTY(160000);

        private final int rate;

        Rate(int r) {
            this.rate = r;
        }
        public int getRate() {
            return this.rate;
        }

        public static Rate fromString(String parameters) {
            if (!parameters.contains("rate")) {
                return UNKNOWN;
            }

            for (String param: parameters.split(";")) {
                if (param.trim().startsWith("rate")) {
                    int rate = Integer.parseInt(param.split("=")[1].trim());
                    for (Rate toMatch: Rate.values()) {
                        if (toMatch.getRate() == rate){
                            return toMatch;
                        }
                    }
                }
            }

            return UNKNOWN;
        }
    }

    //based on IANA standard for audio MIME type:
    // https://www.iana.org/assignments/media-types/media-types.xhtml#audio
    enum Type {
        OTHER(""),
        L16("l16"), //pcm
        OPUS("opus"),
        SPEEX("speex"),
        IMA_ADPCM("adpcm");

        private final String name;
        Type(String name) {
            this.name = name;
        }

        public String getName () {
            return name;
        }

        public static Type fromString(String name) {
            for (Type toMatch: Type.values()) {
                if (toMatch.getName().equalsIgnoreCase(name)) {
                    return toMatch;
                }
            }

            return OTHER;
        }
    }
}
