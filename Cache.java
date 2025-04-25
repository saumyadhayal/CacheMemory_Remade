import java.util.*;

class CacheBlock {
    boolean valid;
    int tag;
    int last_counter;

    CacheBlock() {
        valid = false;      // is the cache valid? (1 = valid, 0 = invalid)
        tag = -1;           // Tag bits associated with the memory address to check if a given memory address is a cache hit or miss
                            // -1 = nothing loaded
        last_counter = 0;   // for replacing last block when set is full
    }
}

public class Cache {
    private int cacheSize;
    private int wordsPerBlock;
    private String mappingPolicy;
    private int nWay;
    private CacheBlock[][] cache;
    private int blocks;
    private int sets;
    private int offsetBits;
    private int indexBits;
    private int tagBits;
    private final int wordSize = 4; // bytes per word

    public void setupCache(Scanner scanner) {
        System.out.print("Enter cache size (in Bytes): ");
        cacheSize = scanner.nextInt();
        System.out.print("Enter words per block (1, 2, 4, 8): ");
        wordsPerBlock = scanner.nextInt();
        System.out.print("Enter mapping (DM or SA): ");
        mappingPolicy = scanner.next().toUpperCase();

        if (mappingPolicy.equals("SA")) {
            System.out.print("Enter number of blocks per set (N-way associative): ");
            nWay = scanner.nextInt();
        } else {
            nWay = 1;
        }

        blocks = cacheSize / (wordsPerBlock * wordSize);
        sets = blocks / nWay;

        offsetBits = (int) (Math.log(wordsPerBlock) / Math.log(2));
        indexBits = sets > 1 ? (int) (Math.log(sets) / Math.log(2)) : 0;
        tagBits = 32 - indexBits - offsetBits;

        cache = new CacheBlock[sets][nWay];
        for (int i = 0; i < sets; i++)
            for (int j = 0; j < nWay; j++)
                cache[i][j] = new CacheBlock();

        System.out.println("\nCache Setup Complete:");
        System.out.println("Blocks: " + blocks);
        System.out.println("Sets: " + sets);
        System.out.println("Address Partition: [Tag: " + tagBits + " bits | Index: " + indexBits + " bits | Offset: " + offsetBits + " bits]");
        System.out.println("Real Cache Size: " + (blocks * wordsPerBlock * wordSize) + " Bytes\n");
    }

    public boolean access(int address){
        return true; //placeholder
    }

    public void clearCache(){
        //placeholder
    }

    public void printStats(){
        // placeholder
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Cache cache = new Cache();
        cache.setupCache(scanner);

        while (true) {
            System.out.println("Menu: ");
            System.out.println("1) Access word address");
            System.out.println("2) Clear Cache");
            System.out.println("3) Simulation mode");
            System.out.println("4) Print stats");
            System.out.println("5) Exit");
            System.out.print("Choose an option (1-5):");
            int choice = scanner.nextInt();
            System.out.println();

            switch (choice) {
                case 1: //access word address
                    System.out.print("Enter word address: ");
                    int wordAddr = scanner.nextInt();
                    int byteAddr = wordAddr * cache.wordSize;
                    boolean hit = cache.access(byteAddr); //method needs to be implemented
                    int blockAddress = byteAddr / (cache.wordSize * cache.wordsPerBlock);
                    int idx;
                    if (cache.indexBits > 0) {
                        idx = blockAddress & ((1 << cache.indexBits) - 1);
                    } else {
                        idx = 0;
                    }
                    int tag = blockAddress >>> cache.indexBits;
                    System.out.printf("Accessing word %d -> byte addr %d: Tag=%d, Index=%d --> %s%n\n", wordAddr, byteAddr, tag, idx, hit ? "Hit" : "Miss");
                    break;
                case 2: //clear cache
                    cache.clearCache(); //method needs to be implemented
                    break;
                    
                case 3: //simulation mode
                    System.out.print("Enter the number of accesses to simulate: ");
                    int n = scanner.nextInt();
                    System.out.print("Enter max word address value (exclusive): ");
                    int maxWord = scanner.nextInt();
                    Random random = new Random();
                    for (int i = 0; i < n; i++){
                        int wa = random.nextInt(maxWord);
                        cache.access(wa * cache.wordSize);
                    }
                    cache.printStats(); //method needs to be implemented
                    System.out.println();
                    break;
                case 4: //stats
                    cache.printStats(); //method needs to be implemented
                    System.out.println();
                    break;
                case 5: // exit
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid Choice. \n");
            }
        
        }

    } 
}
