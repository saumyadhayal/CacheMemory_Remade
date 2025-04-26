import javafx.scene.control.Label;

import java.util.*;

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


    private int globalCounter = 0; // for LRU
    private int hitCount = 0;
    private int missCount = 0;
    private Random random = new Random();

    //record for every access
    private List<AccessRecord> accessLog = new ArrayList<>();

    public int getindexBits() {
        return this.indexBits;
    }

    public int getwordsPerBlock() {
        return wordsPerBlock;
    }

    protected static class AccessRecord {
        int wordAddr, blockNum, offset_block, setNum;
        String mappingPolicy;
        boolean hit;

        AccessRecord(int wordAddr, int blockNum, int offset_block, String mappingPolicy, int setNum, boolean hit) {
            this.wordAddr = wordAddr;
            this.blockNum = blockNum;
            this.offset_block = offset_block;
            this.mappingPolicy = mappingPolicy;
            this.setNum = setNum;
            this.hit = hit;
        }

        public String toString(){
            if (mappingPolicy.equals("SA")) { // set associative
                return String.format("word %d -> set %d, block %d : offset=%d -> %s", 
                wordAddr, setNum, blockNum, offset_block,hit ? "Hit" : "Miss");
            }
            else {
                return String.format(
                "word %d -> blockNum %d : offset-block=%d -> %s",
                wordAddr, blockNum, offset_block, hit ? "Hit" : "Miss");
            }
            
        }
    }

    public void setupCache(int cacheSize, int wordsPerBlock, String mappingPolicy, int nWayInput) {
        this.cacheSize = cacheSize;
        this.wordsPerBlock = wordsPerBlock;
        this.mappingPolicy = mappingPolicy.toUpperCase();
        this.nWay = mappingPolicy.equals("SA") ? nWayInput : 1;

        blocks = cacheSize / (wordsPerBlock * wordSize);
        sets = blocks / nWay;

        offsetBits = (int) (Math.log(wordsPerBlock) / Math.log(2));
        indexBits = sets > 1 ? (int) (Math.log(sets) / Math.log(2)) : 0;
        tagBits = 32 - indexBits - offsetBits;

        cache = new CacheBlock[sets][nWay];
        for (int i = 0; i < sets; i++)
            for (int j = 0; j < nWay; j++)
                cache[i][j] = new CacheBlock();
    }


    public int getNWay() {
        return nWay;
    }

    public int getSets() {
        return sets;
    }

    public CacheBlock getBlock(int i, int j) {
        return this.cache[i][j];
    }

    public boolean accessWord(int wordAddr){
        int byteAddr = wordAddr * wordSize;
        int blockAddress = byteAddr / (wordSize * wordsPerBlock);
        int blockAddress1 = wordAddr/wordsPerBlock;
        int index;
        if (indexBits > 0) {
            index = blockAddress & ((1 << indexBits) - 1);
        } else {
            index = 0;
        }
        int tag = blockAddress >>> indexBits;

        //search set for a hit
        CacheBlock[] set = cache[index];
        CacheBlock LRUBlock = set[0];
        boolean hit = false;
        for (CacheBlock cb : set) {
            if (cb.valid && cb.tag == tag){
                // this is a hit so update LRU
                cb.last_counter = ++globalCounter;
                hitCount++;
                hit = true;
                cb.accessed=true;
                cb.content=generateContent(wordAddr, blockAddress1);
                break;
            }
            if (!cb.valid) {
                LRUBlock = cb;
                break;
            }

            if (LRUBlock == null || cb.last_counter < LRUBlock.last_counter) {
                LRUBlock = cb;
            }
        }

        if (!hit){
            // this is a miss so replace LRU
            LRUBlock.valid = true;
            LRUBlock.tag = tag;
            LRUBlock.last_counter = ++globalCounter;
            LRUBlock.accessed = true;
            LRUBlock.content = generateContent(wordAddr, blockAddress1);
            missCount++;
        }


        int offset_block = wordAddr % wordsPerBlock;
        int blockNum = wordAddr / wordsPerBlock;
        int setNum = blockNum % sets;

        accessLog.add(new AccessRecord(wordAddr, blockNum, offset_block, this.mappingPolicy, setNum, hit));

        return hit;


    }

    private String generateContent(int wordAddr, int blockAddress) {
        StringBuilder sb = new StringBuilder();
        if(wordAddr%wordsPerBlock != 0){
            wordAddr=(wordAddr/wordsPerBlock) * wordsPerBlock;
        }
        sb.append("B").append(blockAddress).append("(");
        for (int w = 0; w < wordsPerBlock; w++) {
            sb.append("W").append(wordAddr + w);
            if (w != wordsPerBlock - 1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    public void printSetupTo(Label label) {
        String details = String.format(
            "Blocks: %d%n" +
            "Sets: %d%n" +
            "Address Partition: [Tag: %d bits | Index: %d bits | Offset: %d bits]%n" +
            "Real Cache Size: %d Bytes",
            blocks,
            sets,
            tagBits,
            indexBits,
            offsetBits,
            blocks * wordsPerBlock * wordSize
        );
        label.setText(details);
    }
    


    public void clearCache(){
        this.globalCounter = this.hitCount = this.missCount = 0;
        accessLog.clear();
        for (int i = 0; i < sets; i++) {
            for (int j = 0; j < nWay; j++){
                cache[i][j] = new CacheBlock();
            }
        }
        System.out.println("Cache cleared. \n");
    }

    public void printStats(){
        int total = hitCount + missCount;
        double hitRate;
        if (total > 0){
            hitRate = (hitCount * 100.0 / total);
        }
        else {
            hitRate = 0.0;
        }
        System.out.printf("Accesses: %d, Hits: %d, Misses: %d (%.2f%% hit rate)%n",
        total, hitCount, missCount, hitRate);
    }

    public void printAccessLog(){
        if (accessLog.isEmpty()){
            System.out.println("No accesses recorded. \n");
            return;
        }
        System.out.println("Access Log: ");
        for (AccessRecord rec : accessLog) {
            System.out.println(rec);
        }
        System.out.println();
    }

    public AccessRecord getLastAccessRecord() {
        return accessLog.isEmpty() ? null : accessLog.get(accessLog.size() - 1);
    }

    public void printStatsTo(Label label) {
        int total = hitCount + missCount;
        double hitRate = total > 0 ? (hitCount * 100.0 / total) : 0.0;
        String stats = String.format(
                "Accesses: %d, Hits: %d, Misses: %d (%.2f%% hit rate)",
                total, hitCount, missCount, hitRate
        );
        label.setText(stats);
    }


    public void printAccessLogTo(Label label) {
        if (accessLog.isEmpty()) {
            label.setText("No accesses recorded.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Access Log:\n");
            for (AccessRecord rec : accessLog) {
                sb.append(rec.toString()).append("\n");
            }
            label.setText(sb.toString());
        }
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
            System.out.println("4) Print Stats");
            System.out.println("5) Print Access Log");
            System.out.println("6) Exit");
            System.out.print("Choose an option (1-6):");
            int choice = scanner.nextInt();
            System.out.println();

            switch (choice) {
                case 1: //access word address
                    System.out.print("Enter word address: ");
                    int wa = scanner.nextInt();
                    boolean hit = cache.accessWord(wa);
                    System.out.println(cache.accessLog.get(cache.accessLog.size()-1));
                    System.out.println();
                    break;
                case 2: //clear cache
                    cache.clearCache();
                    break;
                    
                case 3: //simulation mode
                    System.out.print("Enter the number of accesses to simulate: ");
                    int n = scanner.nextInt();
                    System.out.print("Enter max word address value (exclusive): ");
                    int maxWord = scanner.nextInt();
                    System.out.print("Simulation Type: 1) Random 2) Locality-Based: ");
                    int simType = scanner.nextInt();
                    Random random = new Random();
                    if (simType == 1) {
                        for (int i = 0; i < n; i++){
                        cache.accessWord(random.nextInt(maxWord));
                        }
                    }
                    else if (simType == 2){
                        //locality based here
                    }
                    else {
                        System.out.println("Invalid simulation type, defaulting to random.\n");
                        for (int i = 0; i < n; i++) {
                            cache.accessWord(random.nextInt(maxWord));
                        }
                    }
                    
                    cache.printStats();
                    System.out.println();
                    break;
                case 4: //stats
                    cache.printStats();
                    System.out.println();
                    break;
                case 5: // access log
                    cache.printAccessLog();
                    break;
                case 6: // exit
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid Choice. \n");
            }
        
        }

    }

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
}
