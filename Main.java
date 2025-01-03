import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {


    // Maps for opcode and condition lookup
    private static final Map<Integer, String> OPCODE_MAP = new TreeMap<>();
    private static final Map<Integer, String> CONDITION_MAP = new TreeMap<>();

    // Counter to track the number of instructions processed
    private static int instructionCounter = 1;

    public static void main(String[] arguments) {
        initializeLookupTables(); // Creates mapping.

        if (arguments.length < 1) {
            System.err.println("Usage: java InstructionDisassembler <filename>");
            return;
        }

        String fileName = arguments[0];
        processInstructionFile(fileName);
    }

    private static void initializeLookupTables() {
        OPCODE_MAP.put(0b10001011000, "ADD");
        OPCODE_MAP.put(0b1001000100, "ADDI");
        OPCODE_MAP.put(0b10001010000, "AND");
        OPCODE_MAP.put(0b1001001000, "ANDI");
        OPCODE_MAP.put(0b000101, "B");
        OPCODE_MAP.put(0b01010100, "B.");
        OPCODE_MAP.put(0b100101, "BL");
        OPCODE_MAP.put(0b11010110000, "BR");
        OPCODE_MAP.put(0b10110101, "CBNZ");
        OPCODE_MAP.put(0b10110100, "CBZ");
        OPCODE_MAP.put(0b11001010000, "EOR");
        OPCODE_MAP.put(0b1101001000, "EORI");
        OPCODE_MAP.put(0b11111000010, "LDUR");
        OPCODE_MAP.put(0b11010011011, "LSL");
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

        CONDITION_MAP.put(0x0, "EQ");
        CONDITION_MAP.put(0x1, "NE");
        CONDITION_MAP.put(0x2, "HS");
        CONDITION_MAP.put(0x3, "LO");
        CONDITION_MAP.put(0x4, "MI");
        CONDITION_MAP.put(0x5, "PL");
        CONDITION_MAP.put(0x6, "VS");
        CONDITION_MAP.put(0x7, "VC");
        CONDITION_MAP.put(0x8, "HI");
        CONDITION_MAP.put(0x9, "LS");
        CONDITION_MAP.put(0xA, "GE");
        CONDITION_MAP.put(0xB, "LT");
        CONDITION_MAP.put(0xC, "GT");
        CONDITION_MAP.put(0xD, "LE");
    }

    private static void processInstructionFile(String fileName) {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)))) {
            while (input.available() >= 4) {
                int instruction = readInstruction(input);
                decodeAndPrintInstruction(instruction);
            }
        } catch (IOException exception) {
            System.err.println("Error processing file: " + exception.getMessage());
        }
    }

    private static int readInstruction(DataInputStream input) throws IOException {
        int part1 = (input.readByte() & 0xFF) << 24;
        int part2 = (input.readByte() & 0xFF) << 16;
        int part3 = (input.readByte() & 0xFF) << 8;
        int part4 = input.readByte() & 0xFF;
        return part1 | part2 | part3 | part4;
    }

    private static void decodeAndPrintInstruction(int instruction) {
        String mnemonic = decodeOpcode(instruction >> 21);
        String output = String.format("Instruction %d: %s", instructionCounter++, mnemonic);
        System.out.println(output);
    }

    private static String decodeOpcode(int opcode) {
        return OPCODE_MAP.getOrDefault(opcode, "UNKNOWN");
    }

}