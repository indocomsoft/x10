

package x10.parser;

import com.ibm.lpg.*;
import java.io.*;

class X10Lexerprs implements ParseTable, X10Lexersym {

    public interface IsKeyword {
        public final static byte isKeyword[] = {0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0
        };
    };
    public final static byte isKeyword[] = IsKeyword.isKeyword;
    public final boolean isKeyword(int index) { return isKeyword[index] != 0; }

    public interface BaseCheck {
        public final static byte baseCheck[] = {0,
            1,0,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,2,1,2,
            2,3,2,3,1,2,1,1,1,2,
            1,2,3,4,1,2,1,2,2,3,
            2,3,2,2,2,3,2,3,3,5,
            3,2,2,0,1,2,1,2,2,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,2,3,4,1,2,3,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,2,2,2,2,2,
            2,2,2,2,2,2,2,2,2,3,
            2,2,3,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,2,2,1,1,1,
            1,1,3,2,2,2,2,2,2,2,
            2,2,2,2
        };
    };
    public final static byte baseCheck[] = BaseCheck.baseCheck;
    public final int baseCheck(int index) { return baseCheck[index]; }
    public final static byte rhs[] = baseCheck;
    public final int rhs(int index) { return rhs[index]; };

    public interface BaseAction {
        public final static char baseAction[] = {0,
            19,19,20,20,20,20,20,20,20,20,
            20,28,28,21,32,29,34,34,33,33,
            33,22,22,23,35,35,24,24,37,37,
            5,5,38,38,40,40,25,25,25,25,
            25,25,25,41,41,41,14,14,14,26,
            42,42,42,42,43,43,27,44,44,8,
            8,8,8,8,2,2,2,2,2,3,
            3,3,3,3,3,3,3,3,3,3,
            3,3,3,3,3,3,3,3,3,3,
            3,3,3,3,3,4,4,4,4,4,
            4,4,4,4,4,4,4,4,4,4,
            4,4,4,4,4,4,4,4,4,4,
            4,1,1,1,1,1,1,1,1,1,
            1,10,10,10,10,10,10,10,10,7,
            7,7,7,7,7,7,7,7,7,7,
            7,6,6,46,46,46,46,47,47,47,
            9,9,9,9,13,13,15,15,39,39,
            30,30,30,30,30,30,30,30,30,30,
            30,30,30,30,30,30,30,30,30,30,
            30,30,30,30,30,31,31,31,31,31,
            31,31,31,31,31,31,31,31,31,31,
            31,31,31,48,48,48,48,48,48,48,
            48,48,48,48,48,48,48,48,48,48,
            48,48,48,48,48,48,48,48,48,48,
            48,48,17,17,17,17,17,17,17,17,
            17,17,17,17,17,17,17,17,17,17,
            17,17,17,17,17,17,17,17,17,17,
            18,18,18,18,18,18,18,18,18,18,
            18,18,18,18,18,18,18,18,18,18,
            18,18,18,18,18,18,18,18,49,49,
            49,49,49,49,49,49,49,49,49,49,
            49,49,49,49,49,49,49,49,49,49,
            49,49,49,49,49,49,16,16,16,16,
            51,51,45,45,45,45,45,45,45,45,
            12,12,12,12,12,12,12,36,36,36,
            36,36,36,36,11,11,11,11,11,11,
            11,11,31,31,305,31,19,545,545,461,
            1078,32,17,697,21,20,20,20,203,59,
            59,59,59,446,1,3,4,5,6,7,
            8,9,10,11,546,546,548,374,491,1046,
            31,547,547,915,371,481,1100,32,379,406,
            438,438,438,438,1,53,53,53,53,939,
            438,438,53,59,1035,31,59,438,40,407,
            53,53,600,540,540,540,540,503,26,26,
            26,26,465,540,438,889,959,969,26,26,
            102,51,51,51,51,26,347,451,51,302,
            927,32,994,992,802,557,51,51,540,43,
            35,557,518,30,517,425,827,559,771,36,
            1162,540,487,558,36,36,852,497,944,38,
            1163,34,497,497,420,425,877,500,18,902,
            157,415,500,500,479,157,157,1057,31,354,
            1089,31,520,1089,31,524,968,1144,529,1111,
            32,1153,353,1122,32,42,1003,522,1133,32,
            978,160,1012,1014,1017,1167,1019,977,1170,784,
            768,1171,303,380,400,401,613,709,1172,1174,
            1175,1184,1186,1189,1190,1191,710,1193,1194,562,
            562
        };
    };
    public final static char baseAction[] = BaseAction.baseAction;
    public final int baseAction(int index) { return baseAction[index]; }
    public final static char lhs[] = baseAction;
    public final int lhs(int index) { return lhs[index]; };

    public interface TermCheck {
        public final static byte termCheck[] = {0,
            0,1,2,3,4,5,6,7,8,9,
            10,11,12,13,14,15,16,17,18,19,
            20,21,22,23,24,25,26,27,28,29,
            30,31,32,33,34,35,36,37,38,39,
            40,41,42,43,44,45,46,47,48,49,
            50,51,52,53,54,55,56,57,58,59,
            60,61,62,63,64,65,66,67,68,69,
            70,71,72,73,74,75,76,77,78,79,
            80,81,82,83,84,85,86,87,88,89,
            90,91,92,93,94,95,96,97,98,99,
            100,0,1,2,3,4,5,6,7,8,
            9,10,11,12,13,14,15,16,17,18,
            19,20,21,22,23,24,25,26,27,28,
            29,30,31,32,33,34,35,36,37,38,
            39,40,41,42,43,44,45,46,47,48,
            49,50,51,52,53,54,55,56,57,58,
            59,60,61,62,63,64,65,66,67,68,
            69,70,71,72,73,74,75,76,77,78,
            79,80,81,82,83,84,85,86,87,88,
            89,90,91,92,93,94,95,96,97,98,
            99,100,0,1,2,3,4,5,6,7,
            8,9,10,11,12,13,14,15,16,17,
            18,19,20,21,22,23,24,25,26,27,
            28,29,30,31,32,33,34,35,36,37,
            38,39,40,41,42,43,44,45,46,47,
            48,49,50,51,52,53,54,55,56,57,
            58,59,60,61,62,63,64,65,66,67,
            68,69,70,71,72,73,74,75,76,77,
            78,79,80,81,82,83,84,85,86,87,
            88,89,90,91,92,93,94,95,96,97,
            98,0,0,101,0,1,2,3,4,5,
            6,7,8,9,10,11,12,13,14,15,
            16,17,18,19,20,21,22,23,24,25,
            26,27,28,29,30,31,32,33,34,35,
            36,37,38,39,40,41,42,43,44,45,
            46,47,48,49,50,51,52,53,54,55,
            56,57,58,59,60,61,62,63,64,65,
            66,67,68,69,70,71,72,73,74,0,
            76,77,78,79,80,81,82,83,84,85,
            86,87,88,89,90,91,92,93,94,0,
            0,97,98,99,100,0,1,2,3,4,
            5,6,7,8,9,10,11,12,13,14,
            15,16,17,18,19,20,21,22,23,24,
            25,26,27,28,29,30,31,32,33,34,
            35,36,37,38,39,40,41,42,43,44,
            45,46,47,48,49,50,51,52,53,54,
            55,56,57,58,59,60,61,62,63,64,
            65,66,67,68,69,70,71,72,73,74,
            75,76,77,78,79,80,81,82,83,84,
            85,86,87,88,89,90,91,92,93,94,
            95,96,0,1,2,3,4,5,6,7,
            8,9,10,11,12,13,14,15,16,17,
            18,19,20,21,22,23,24,25,26,27,
            28,29,30,31,32,33,34,35,36,37,
            38,39,40,41,42,43,44,45,46,47,
            48,49,50,51,52,53,54,55,56,57,
            58,59,60,61,62,63,64,65,66,67,
            68,69,70,71,72,73,74,75,76,77,
            78,79,80,81,82,83,84,85,86,87,
            88,89,90,91,92,93,94,95,96,0,
            1,2,3,4,5,6,7,8,9,10,
            11,12,0,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45,46,47,48,49,50,
            51,52,53,54,55,56,57,58,59,60,
            61,62,63,64,65,66,67,68,69,70,
            71,72,73,74,75,76,77,78,79,80,
            81,82,83,84,85,86,87,88,89,90,
            91,92,93,94,95,96,0,1,2,3,
            4,5,6,7,8,9,10,11,0,0,
            14,15,16,17,18,19,20,21,22,23,
            24,25,26,27,28,29,30,0,32,33,
            0,0,0,37,38,39,40,41,42,43,
            44,45,46,47,48,49,50,51,52,53,
            54,55,56,57,58,59,60,61,62,63,
            64,65,66,67,68,69,70,0,0,73,
            0,1,2,3,4,5,6,7,8,9,
            10,11,0,0,14,15,16,17,18,19,
            20,21,22,23,24,25,13,0,31,0,
            30,0,1,2,3,4,5,6,7,8,
            9,10,11,0,0,14,15,16,17,18,
            19,20,21,22,23,24,0,1,2,3,
            4,5,6,7,8,9,10,11,0,0,
            14,15,16,17,18,19,20,21,22,23,
            24,0,1,2,3,4,5,6,7,8,
            9,10,11,0,0,14,15,16,17,18,
            19,20,21,22,23,24,0,1,2,3,
            4,5,6,7,8,9,10,11,0,0,
            14,15,16,17,18,19,20,21,22,23,
            24,0,1,2,3,4,5,6,7,8,
            9,10,11,0,0,14,15,16,17,18,
            19,20,21,22,23,24,0,1,2,3,
            4,5,6,7,8,9,10,11,0,0,
            14,15,16,0,18,19,32,33,0,11,
            0,25,14,15,16,12,30,31,0,1,
            2,3,4,5,6,7,8,0,0,11,
            0,13,0,0,0,17,0,0,11,0,
            12,14,15,16,26,27,28,29,12,12,
            102,0,34,0,1,2,3,4,5,6,
            7,8,0,12,11,0,13,0,0,0,
            17,0,36,0,12,0,0,74,0,26,
            27,28,29,12,0,12,0,34,12,0,
            12,0,0,75,0,1,2,3,4,5,
            6,7,8,9,10,0,1,2,3,4,
            5,6,7,8,9,10,0,1,2,3,
            4,5,6,7,8,9,10,76,75,35,
            36,0,0,0,72,0,31,0,1,2,
            3,4,5,6,7,8,9,10,0,1,
            2,3,4,5,6,7,8,9,10,0,
            1,2,3,4,5,6,7,8,9,10,
            0,1,2,3,4,5,6,7,8,9,
            10,0,1,2,3,4,5,6,7,8,
            9,10,0,1,2,3,4,5,6,7,
            8,9,10,0,1,2,3,4,5,6,
            7,8,0,1,2,3,4,5,6,7,
            8,0,0,0,0,0,0,0,0,0,
            0,0,11,0,0,14,15,16,12,18,
            19,12,12,0,13,0,13,13,0,0,
            0,0,0,0,0,0,13,0,13,0,
            0,13,13,13,35,13,13,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,71,0,0,0,0,0,0,
            0,0,0,77,78,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,97,
            98,99,100,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0
        };
    };
    public final static byte termCheck[] = TermCheck.termCheck;
    public final int termCheck(int index) { return termCheck[index]; }

    public interface TermAction {
        public final static char termAction[] = {0,
            562,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,614,617,615,
            615,615,615,615,615,615,615,615,615,615,
            615,615,615,615,615,615,615,615,615,615,
            615,562,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,612,618,
            613,613,613,613,613,613,613,613,613,613,
            613,613,613,613,613,613,613,613,613,613,
            613,613,57,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,621,621,621,621,621,621,621,621,621,
            621,54,17,621,2,404,544,544,544,544,
            544,544,544,544,544,545,535,433,545,545,
            545,545,545,545,545,545,545,545,545,545,
            545,545,545,545,545,400,545,545,410,539,
            538,545,545,545,545,545,545,545,545,545,
            545,545,545,545,545,545,545,545,545,545,
            545,545,545,545,545,545,545,545,545,545,
            545,545,545,545,545,543,527,545,489,31,
            464,536,537,531,448,757,745,533,534,742,
            754,755,752,753,756,740,749,737,738,19,
            10,543,543,543,543,562,438,438,438,438,
            438,438,438,438,438,438,438,438,438,438,
            438,438,438,438,438,438,438,438,438,438,
            438,438,438,438,438,438,438,438,438,585,
            438,438,438,438,438,438,438,438,438,438,
            438,438,438,438,438,438,438,438,438,438,
            438,438,438,438,438,438,438,438,438,438,
            438,438,438,438,438,438,438,438,438,438,
            447,438,438,438,438,438,438,438,438,438,
            438,438,438,438,438,438,438,438,438,438,
            438,438,562,588,588,588,588,588,588,588,
            588,588,588,588,588,588,588,588,588,588,
            588,588,588,588,588,588,588,588,588,588,
            588,588,588,588,588,588,584,588,588,588,
            588,588,588,588,588,588,588,588,588,588,
            588,588,588,588,588,588,588,588,588,588,
            588,588,588,588,588,588,588,588,588,588,
            588,588,588,588,588,588,588,447,588,588,
            588,588,588,588,588,588,588,588,588,588,
            588,588,588,588,588,588,588,588,588,562,
            540,540,540,540,540,540,540,540,540,540,
            540,540,6,540,540,540,540,540,540,540,
            540,540,540,540,540,540,540,540,540,540,
            540,540,540,540,540,540,540,540,540,540,
            540,540,540,540,540,540,540,540,540,540,
            540,540,540,540,540,540,540,540,540,540,
            540,540,540,540,540,540,540,540,540,540,
            540,540,540,540,463,540,540,540,540,540,
            540,540,540,540,540,540,540,540,540,540,
            540,540,540,540,540,540,15,583,583,583,
            583,583,583,583,583,583,583,582,3,35,
            582,582,582,582,582,582,582,582,582,582,
            582,582,582,582,582,582,582,562,582,582,
            562,562,562,582,582,582,582,582,582,582,
            582,582,582,582,582,582,582,582,582,582,
            582,582,582,582,582,582,582,582,582,582,
            582,582,582,582,582,582,582,363,562,582,
            33,598,598,598,598,598,598,598,598,598,
            598,598,562,562,598,598,598,598,598,598,
            598,598,598,598,598,596,586,562,775,562,
            596,562,557,557,557,557,557,557,557,557,
            557,557,557,562,562,557,557,557,557,557,
            557,557,557,557,557,557,562,559,559,559,
            559,559,559,559,559,559,559,558,562,562,
            558,558,558,558,558,558,558,558,558,558,
            558,154,497,497,497,497,497,497,497,497,
            497,497,497,562,562,497,497,497,497,497,
            497,497,497,497,497,497,155,500,500,500,
            500,500,500,500,500,500,500,500,562,562,
            500,500,500,500,500,500,500,500,500,500,
            500,156,719,719,719,719,719,719,719,719,
            719,719,719,562,31,719,719,719,719,719,
            719,719,719,719,719,719,29,594,594,594,
            594,594,594,594,594,594,594,605,39,562,
            605,605,605,185,425,425,465,465,562,602,
            562,592,602,602,602,768,592,508,562,909,
            909,909,909,909,909,909,909,41,189,920,
            562,923,562,562,562,917,172,182,604,562,
            762,604,604,604,919,921,918,908,765,771,
            561,186,922,562,549,550,551,552,553,554,
            555,556,184,761,920,562,923,562,562,562,
            917,181,759,179,769,562,177,774,173,919,
            921,918,477,770,562,926,562,922,760,562,
            766,562,562,924,562,544,544,544,544,544,
            544,544,544,544,544,188,544,544,544,544,
            544,544,544,544,544,544,45,544,544,544,
            544,544,544,544,544,544,544,542,924,514,
            511,562,562,562,773,562,541,44,594,594,
            594,594,594,594,594,594,594,594,562,544,
            544,544,544,544,544,544,544,544,544,47,
            594,594,594,594,594,594,594,594,594,594,
            46,594,594,594,594,594,594,594,594,594,
            594,49,594,594,594,594,594,594,594,594,
            594,594,48,594,594,594,594,594,594,594,
            594,594,594,158,522,522,522,522,522,522,
            522,522,159,722,722,722,722,722,722,722,
            722,37,16,562,562,562,174,562,562,171,
            201,132,600,133,134,600,600,600,767,425,
            425,764,772,135,158,136,158,158,137,138,
            139,562,153,152,562,562,158,562,158,562,
            562,158,158,158,758,154,154,562,562,562,
            562,562,562,562,562,562,562,562,562,562,
            562,562,562,562,562,562,562,562,562,562,
            562,562,562,580,562,562,562,562,562,562,
            562,562,562,620,460,562,562,562,562,562,
            562,562,562,562,562,562,562,562,562,580,
            580,580,580
        };
    };
    public final static char termAction[] = TermAction.termAction;
    public final int termAction(int index) { return termAction[index]; }
    public final int asb(int index) { return 0; }
    public final int asr(int index) { return 0; }
    public final int nasb(int index) { return 0; }
    public final int nasr(int index) { return 0; }
    public final int terminalIndex(int index) { return 0; }
    public final int nonterminalIndex(int index) { return 0; }
    public final int scopePrefix(int index) { return 0;}
    public final int scopeSuffix(int index) { return 0;}
    public final int scopeLhs(int index) { return 0;}
    public final int scopeLa(int index) { return 0;}
    public final int scopeStateSet(int index) { return 0;}
    public final int scopeRhs(int index) { return 0;}
    public final int scopeState(int index) { return 0;}
    public final int inSymb(int index) { return 0;}
    public final String name(int index) { return null; }
    public final int getErrorSymbol() { return 0; }
    public final int getScopeUbound() { return 0; }
    public final int getScopeSize() { return 0; }
    public final int getMaxNameLength() { return 0; }

    public final static int
           NUM_STATES        = 68,
           NT_OFFSET         = 102,
           LA_STATE_OFFSET   = 926,
           MAX_LA            = 1,
           NUM_RULES         = 364,
           NUM_NONTERMINALS  = 51,
           NUM_SYMBOLS       = 153,
           SEGMENT_SIZE      = 8192,
           START_STATE       = 365,
           IDENTIFIER_SYMBOL = 0,
           EOFT_SYMBOL       = 102,
           EOLT_SYMBOL       = 103,
           ACCEPT_ACTION     = 561,
           ERROR_ACTION      = 562;

    public final static boolean BACKTRACK = false;

    public final int getNumStates() { return NUM_STATES; }
    public final int getNtOffset() { return NT_OFFSET; }
    public final int getLaStateOffset() { return LA_STATE_OFFSET; }
    public final int getMaxLa() { return MAX_LA; }
    public final int getNumRules() { return NUM_RULES; }
    public final int getNumNonterminals() { return NUM_NONTERMINALS; }
    public final int getNumSymbols() { return NUM_SYMBOLS; }
    public final int getSegmentSize() { return SEGMENT_SIZE; }
    public final int getStartState() { return START_STATE; }
    public final int getIdentifierSymbol() { return IDENTIFIER_SYMBOL; }
    public final int getEoftSymbol() { return EOFT_SYMBOL; }
    public final int getEoltSymbol() { return EOLT_SYMBOL; }
    public final int getAcceptAction() { return ACCEPT_ACTION; }
    public final int getErrorAction() { return ERROR_ACTION; }
    public final boolean isValidForParser() { return isValidForParser; }
    public final boolean getBacktrack() { return BACKTRACK; }

    public final int originalState(int state) { return 0; }
    public final int asi(int state) { return 0; }
    public final int nasi(int state) { return 0; }
    public final int inSymbol(int state) { return 0; }

    public final int ntAction(int state, int sym) {
        return baseAction[state + sym];
    }

    public final int tAction(int state, int sym) {
        int i = baseAction[state],
            k = i + sym;
        return termAction[termCheck[k] == sym ? k : i];
    }
    public final int lookAhead(int la_state, int sym) {
        int k = la_state + sym;
        return termAction[termCheck[k] == sym ? k : la_state];
    }
}
