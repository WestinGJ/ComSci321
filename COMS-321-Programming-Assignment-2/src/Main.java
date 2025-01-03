import java.io.*;
import java.util.HashMap;

public class Main {
    private static final HashMap<Integer, String> OPCODE_MAP = new HashMap<>();
    private static final HashMap<Integer, String> BRANCH_CONDITION_MAP = new HashMap<>();
    private static final int RorDTypeOpcodeSize = 0x7FF;
    private static final int ITypeOpcodeSize = 0x3FF;
    private static final int CBTypeOpcodeSize = 0xFF;
    private static final int BTypeOpcodeSize = 0x3F;
    private static final int RnSize = 0x1F;
    private static final int RdSize = 0x1F;
    private static final int RtSize = 0x1F;

    public static void main(String[] args) {
        populateOpcodeMap();
        populateBranchConditionMap();

        String filePath = args[0];
        File file = new File(filePath);
        parseInstructionFile(file);
    }

    public static void parseInstructionFile(File file) {
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());

            if (fileBytes.length % 4 != 0) {
                throw new IOException("File length is not a multiple of 4 bytes.");
            }

            for (int i = 0, counter = 1; i < fileBytes.length; i += 4, counter++) {
                int instruction = ((fileBytes[i] & 0xFF) << 24) |
                        ((fileBytes[i + 1] & 0xFF) << 16) |
                        ((fileBytes[i + 2] & 0xFF) << 8) |
                        (fileBytes[i + 3] & 0xFF);

                decodeInstruction(instruction, counter);
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void decodeInstruction(int instruction, int counter) {
        final int ROR_D_SHIFT = 21;
        final int I_TYPE_SHIFT = 22;
        final int B_TYPE_SHIFT = 26;
        final int CB_TYPE_SHIFT = 24;
        final int B_TYPE_MASK = 0x3FFFFFF;
        final int B_TYPE_OFFSET = 0x2000000;
        final int B_TYPE_LIMIT = 0x4000000;

        StringBuilder instructionString = new StringBuilder();

        // Extract opcodes
        int rOrDType = (instruction >> ROR_D_SHIFT) & RorDTypeOpcodeSize;
        int iType = (instruction >> I_TYPE_SHIFT) & ITypeOpcodeSize;
        int bType = (instruction >> B_TYPE_SHIFT) & BTypeOpcodeSize;
        int cbType = (instruction >> CB_TYPE_SHIFT) & CBTypeOpcodeSize;

        // Decode instruction based on opcode
        if (tryDecodeInstruction(rOrDType, instruction, instructionString, DecodeType.R_OR_D)) {
            decodeR_DInstructions(rOrDType, instruction, instructionString);
        } else if (tryDecodeInstruction(iType, instruction, instructionString, DecodeType.I_TYPE)) {
            decodeIInstructions(instruction, instructionString);
        } else if (tryDecodeInstruction(cbType, instruction, instructionString, DecodeType.CB_TYPE)) {
            decodeCBInstructions(cbType, instruction, counter, instructionString);
        } else if (OPCODE_MAP.containsKey(bType)) {
            int address = calculateImmediate(instruction & B_TYPE_MASK, B_TYPE_OFFSET, B_TYPE_LIMIT);
            instructionString.append(OPCODE_MAP.get(bType))
                    .append(" Line ")
                    .append(counter + address);
        } else {
            instructionString.append("ERROR");
        }

        // Print Assembly Instruction
        System.out.println("Line " + counter + ": " + instructionString);
    }

    private enum DecodeType { R_OR_D, I_TYPE, CB_TYPE }

    private static boolean tryDecodeInstruction(int opcode, int instruction, StringBuilder instructionString, DecodeType type) {
        if (OPCODE_MAP.containsKey(opcode)) {
            instructionString.append(OPCODE_MAP.get(opcode));
            return true;
        }
        return false;
    }

    private static void decodeR_DInstructions(int opcode, int instruction, StringBuilder result) {
        if (isThreeFieldInstruction(opcode)) {
            appendFields(result, getRegister(instruction & 0x1F), getRegister((instruction >> 5) & 0x1F), getRegister((instruction >> 16) & 0x1F));
        } else if (opcode == 0b11010110000) {
            result.append(" ").append(getRegister((instruction >> 5) & 0x1F));
        } else if (isShiftInstruction(opcode)) {
            String Rd = getRegister(instruction & RdSize);
            String Rn = getRegister((instruction >> 5) & RnSize);
            int shamt = calculateImmediate((instruction >> 10) & 0x3F, 0x20, 0x40);
            result.append(" ").append(Rd).append(", ").append(Rn).append(", #").append(shamt);
        } else if (opcode == 0b11111111101) {
            result.append(" ").append(getRegister(instruction & 0x1F));
        } else if (isLoadStoreInstruction(opcode)) {
            String Rt = getRegister(instruction & RtSize);
            String Rn = getRegister((instruction >> 5) & RnSize);
            int address = calculateImmediate((instruction >> 12) & 0x1FF, 0x100, 0x200);
            result.append(" ").append(Rt).append(", [").append(Rn).append(", #").append(address).append("]");
        }
    }

    private static void decodeIInstructions(int instruction, StringBuilder result) {
        String Rd = getRegister(instruction & RdSize);
        String Rn = getRegister((instruction >> 5) & RnSize);

        int immediate = calculateImmediate(((instruction >> 10) & 0xFFF), 0x800, 0x1000);
        appendFields(result, Rd, Rn, "#" + immediate);
    }

    private static void decodeCBInstructions(int opcode, int instruction, int counter, StringBuilder result) {
        if (opcode == 0b01010100) {
            int Rt = instruction & RtSize;
            result.append(BRANCH_CONDITION_MAP.get(Rt));
        }
        int address = calculateImmediate((instruction >> 5) & 0x7FFFF, 0x40000, 0x80000);
        result.append(" Line ").append(counter + address);
    }

    private static void appendFields(StringBuilder result, String... fields) {
        result.append(" ").append(String.join(", ", fields));
    }

    private static String getRegister(int regCode) {
        return switch (regCode) {
            case 28 -> "SP"; // Special cases
            case 29 -> "FP";
            case 30 -> "LR";
            case 31 -> "XZR";
            default -> "X" + regCode;
        };
    }

    private static int calculateImmediate(int immediate, int positiveThreshold, int negativeOffset) {
        if (immediate >= positiveThreshold) {
            return immediate - negativeOffset;
        }
        return immediate;
    }

    private static boolean isThreeFieldInstruction(int opcode) {
        return switch (opcode) {
            case 0b10001011000, 0b10001010000, 0b11001010000, 0b10101010000,
                    0b11001011000, 0b11101011000, 0b10011011000 -> true;
            default -> false;
        };
    }

    private static boolean isShiftInstruction(int opcode) {
        return opcode == 0b11010011011 || opcode == 0b11010011010;
    }

    private static boolean isLoadStoreInstruction(int opcode) {
        return opcode == 0b11111000010 || opcode == 0b11111000000;
    }

    private static void populateOpcodeMap() {
        OPCODE_MAP.put(0b10001011000, "ADD");
        OPCODE_MAP.put(0b1001000100, "ADDI");
        OPCODE_MAP.put(0b10001010000, "AND");
        OPCODE_MAP.put(0b1001001000, "ANDI");
        OPCODE_MAP.put(0b000101, "B");
        OPCODE_MAP.put(0b01010100, "B."); // This is a CB instruction in which the Rt field is not a register, but a code that indicates the condition extension.
        OPCODE_MAP.put(0b100101, "BL");
        OPCODE_MAP.put(0b11010110000, "BR");
        OPCODE_MAP.put(0b10110101, "CBNZ");
        OPCODE_MAP.put(0b10110100, "CBZ");
        OPCODE_MAP.put(0b11001010000, "EOR");
        OPCODE_MAP.put(0b1101001000, "EORI");
        OPCODE_MAP.put(0b11111000010, "LDUR");
        OPCODE_MAP.put(0b11010011011, "LSL"); // This instruction uses the shamt field to encode the shift amount, while Rm is unused.
        OPCODE_MAP.put(0b11010011010, "LSR");
        OPCODE_MAP.put(0b10101010000, "ORR");
        OPCODE_MAP.put(0b1011001000, "ORRI");
        OPCODE_MAP.put(0b11111000000, "STUR");
        OPCODE_MAP.put(0b11001011000, "SUB");
        OPCODE_MAP.put(0b1101000100, "SUBI");
        OPCODE_MAP.put(0b1111000100, "SUBIS");
        OPCODE_MAP.put(0b11101011000, "SUBS");
        OPCODE_MAP.put(0b10011011000, "MUL");
        OPCODE_MAP.put(0b11111111101, "PRNT");
        OPCODE_MAP.put(0b11111111100, "PRNL");
        OPCODE_MAP.put(0b11111111110, "DUMP");
        OPCODE_MAP.put(0b11111111111, "HALT");
    }

    private static void populateBranchConditionMap() {
        BRANCH_CONDITION_MAP.put(0x0, "EQ");
        BRANCH_CONDITION_MAP.put(0x1, "NE");
        BRANCH_CONDITION_MAP.put(0x2, "HS");
        BRANCH_CONDITION_MAP.put(0x3, "LO");
        BRANCH_CONDITION_MAP.put(0x4, "MI");
        BRANCH_CONDITION_MAP.put(0x5, "PL");
        BRANCH_CONDITION_MAP.put(0x6, "VS");
        BRANCH_CONDITION_MAP.put(0x7, "VC");
        BRANCH_CONDITION_MAP.put(0x8, "HI");
        BRANCH_CONDITION_MAP.put(0x9, "LS");
        BRANCH_CONDITION_MAP.put(0xa, "GE");
        BRANCH_CONDITION_MAP.put(0xb, "LT");
        BRANCH_CONDITION_MAP.put(0xc, "GT");
        BRANCH_CONDITION_MAP.put(0xd, "LE");
    }

}