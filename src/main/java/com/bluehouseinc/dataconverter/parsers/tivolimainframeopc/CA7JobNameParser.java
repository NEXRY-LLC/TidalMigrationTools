package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

public class CA7JobNameParser {
    
    public static class ParsedJobName {
        private String originalValue;
        private String groupName;
        private String category;
        private String jobName;
        private String jobSequence;
        
        public ParsedJobName(String originalValue, String groupName, String category, String jobName, String jobSequence) {
            this.originalValue = originalValue;
            this.groupName = groupName;
            this.category = category;
            this.jobName = jobName;
            this.jobSequence = jobSequence;
        }
        
        // Getters
        public String getOriginalValue() { return originalValue; }
        public String getGroupName() { return groupName; }
        public String getCategory() { return category; }
        public String getJobName() { return jobName; }
        public String getJobSequence() { return jobSequence; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Original: " + originalValue + " -> ");
            sb.append("Group: ").append(groupName);
            if (category != null) sb.append(", Category: ").append(category);
            if (jobName != null) sb.append(", Job: ").append(jobName);
            if (jobSequence != null) sb.append(", Sequence: ").append(jobSequence);
            return sb.toString();
        }
    }
    
    /**
     * Parses a job name string in format: GROUP, GROUP#CATEGORY, GROUP#CATEGORY#JOBNAME, or GROUP#CATEGORY#JOBNAME#SEQUENCE
     * @param nameString The string to parse (e.g., "DSSP", "DSSP#MISC", "DSSP#MISC#SUN", or "DSSP#MISC#SUN#01")
     * @return ParsedJobName object with extracted components (category, jobName and sequence will be null if not provided)
     * @throws IllegalArgumentException if format is invalid
     */
    public static ParsedJobName parseJobName(String nameString) {
        if (nameString == null || nameString.trim().isEmpty()) {
            throw new IllegalArgumentException("Name string cannot be null or empty");
        }
        
        String[] parts = nameString.split("#");
        
        if (parts.length < 1 || parts.length > 4) {
            throw new IllegalArgumentException(
                String.format("Invalid format. Expected 'GROUP', 'GROUP#CATEGORY', 'GROUP#CATEGORY#JOBNAME', or 'GROUP#CATEGORY#JOBNAME#SEQUENCE', got: %s", nameString)
            );
        }
        
        String groupName = parts[0].trim();
        String category = null;
        String jobName = null;
        String jobSequence = null;
        
        // Parse category if provided
        if (parts.length >= 2) {
            category = parts[1].trim();
            if (category.isEmpty()) {
                category = null;
            }
        }
        
        // Parse job name if provided
        if (parts.length >= 3) {
            jobName = parts[2].trim();
            if (jobName.isEmpty()) {
                jobName = null;
            }
        }
        
        // Parse sequence if provided
        if (parts.length == 4) {
            jobSequence = parts[3].trim();
            if (jobSequence.isEmpty()) {
                jobSequence = null;
            }
        }
        
        // Validate that group name is not empty
        if (groupName.isEmpty()) {
            throw new IllegalArgumentException("Group name must be non-empty");
        }
        
        return new ParsedJobName(nameString, groupName, category, jobName, jobSequence);
    }
    
    // Example usage and testing
    public static void main(String[] args) {
        try {
            // Test your example: DSSP#MISC#SUN#01
            ParsedJobName parsed1 = parseJobName("DSSP#MISC#SUN#01");
            System.out.println(parsed1);
            System.out.println("Original: " + parsed1.getOriginalValue());
            System.out.println("Group: " + parsed1.getGroupName());
            System.out.println("Category: " + parsed1.getCategory());
            System.out.println("Job: " + parsed1.getJobName());
            System.out.println("Sequence: " + parsed1.getJobSequence());
            
            // Test group reference only
            ParsedJobName parsed2 = parseJobName("DSSP");
            System.out.println("\n" + parsed2);
            
            // Test group and category
            ParsedJobName parsed3 = parseJobName("DSSP#MISC");
            System.out.println("\n" + parsed3);
            
            // Test group, category, and job without sequence
            ParsedJobName parsed4 = parseJobName("DSSP#MISC#SUN");
            System.out.println("\n" + parsed4);
            
            // Test other valid inputs
            System.out.println("\nOther examples:");
            System.out.println(parseJobName("PROD"));
            System.out.println(parseJobName("PROD#BATCH"));
            System.out.println(parseJobName("PROD#BATCH#NIGHTLY"));
            System.out.println(parseJobName("PROD#BATCH#NIGHTLY#05"));
            System.out.println(parseJobName("TEST#REQ#VALIDATION#001"));
            
        } catch (IllegalArgumentException e) {
            System.err.println("Parse error: " + e.getMessage());
        }
        
        // Test error cases
        System.out.println("\nTesting error cases:");
        testErrorCase("");                           // Empty string
        testErrorCase("   ");                        // Whitespace only
        testErrorCase("#");                          // Empty group name
        testErrorCase("##");                         // All empty
        testErrorCase(null);                         // Null input
        testErrorCase("A#B#C#D#E");                 // Too many parts
    }
    
    private static void testErrorCase(String input) {
        try {
            parseJobName(input);
            System.out.println("Unexpected success for: " + input);
        } catch (IllegalArgumentException e) {
            System.out.println("Expected error for '" + input + "': " + e.getMessage());
        }
    }
}