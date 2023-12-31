import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Random.*;

public class ParkingTestGenerator {
    private final Random rand = new Random();
    private Path inPath = null;
    private Path outPath = null;
    private int prevTime = -1;
    private List<String> textToWriteInFile = new ArrayList<>();
    private List<String> textToWriteOutFile = new ArrayList<>();
    private List<String> generatedRegistrationsForToday = new ArrayList<>();
    private List<String> generatedRegistrationsForTomorrow = new ArrayList<>();
    private Map<String, String[]> generatedPayments = new HashMap<>();

    private boolean createPaths(){
        try {
            inPath = Paths.get("test.in");
        }
        catch(Exception e){
            System.out.println("Failed to find the specified inPath.");
            return false;
        }
        try {
            outPath = Paths.get("test.out");
        }
        catch(Exception e){
            System.out.println("Failed to find the specified outPath.");
            return false;
        }

        return true;
    }

    private String createValidRegistration(){
        StringBuilder registration = new StringBuilder();
        registration.append((char)(rand.nextInt(26) + 'A'));
        int sizeLimit = rand.nextInt(11);
        if(sizeLimit < 2){sizeLimit = 2;}
        for(int i = 0; i < sizeLimit; ++i){
            switch (rand.nextInt(2)) {
                case 0 -> registration.append((char) (rand.nextInt(26) + 'A'));
                case 1 -> registration.append((char) (rand.nextInt(10) + '0'));
                default -> System.out.println("Failed to append new character to the registration");
            }
        }

        return registration.toString();
    }

    private String createInvalidRegistration(){
        int typeOfInvalidString = rand.nextInt(2);
        if(typeOfInvalidString == 1){//Let's make a valid Registration and then
            //make it invalid
            StringBuilder registration = new StringBuilder(createValidRegistration());
            int appendingLimit = rand.nextInt(100);
            ++appendingLimit;
            for(int i = 0; i < appendingLimit; ++i){
                registration.append((char)(rand.nextInt('z'-'0') + '0'));
            }
            registration.append('!');
            return registration.toString();
        }
        else{//let's build an invalid string from the ground-up
            StringBuilder registration = new StringBuilder();
            int appendingLimit = rand.nextInt(100);
            ++appendingLimit;
            for(int i = 0; i < appendingLimit; ++i){
                registration.append((char)(rand.nextInt('z'-'0') + '0'));
            }
            registration.append('!');
            return registration.toString();
        }
    }

    private String createRegistration(int typeOfRegistration){
        if(typeOfRegistration != 1){//We will make a test with a valid registration
            return createValidRegistration();
        }
        else{//We will make a test with an invalid registration
            return createInvalidRegistration();
        }
    }

    private String useExistingRegistration(int dayToUse){
        if(dayToUse == 0){//test for today
            if(!generatedRegistrationsForToday.isEmpty()){
                int iter = rand.nextInt(generatedRegistrationsForToday.size());
                return generatedRegistrationsForToday.get(iter);
            }
            else{
                return "";
            }
        }
        else {//test for tomorrow
            if(!generatedRegistrationsForTomorrow.isEmpty()){
                int iter = rand.nextInt(generatedRegistrationsForTomorrow.size());
                return generatedRegistrationsForTomorrow.get(iter);
            }
            else{
                return "";
            }
        }
    }

    private String createValidHour(){
        int hourLength = rand.nextInt(2);
        ++hourLength;// We want hour to have the length of 1 or 2, not 0 or 1.
        StringBuilder hour = new StringBuilder();
        if(hourLength == 1){hour.append((char)(rand.nextInt(2) + '8'));}
        else{
            switch(rand.nextInt(3)){
                case 0 -> hour.append('0').append((char)(rand.nextInt(2) + '8'));
                case 1 -> hour.append('1').append((char)(rand.nextInt(10) + '0'));
                case 2 -> hour.append("20");
                default -> System.out.println("Couldn't append to the valid hour");
            }
        }
        hour.append('.').append((char)(rand.nextInt(6) + '0')).append((char)(rand.nextInt(10) + '0'));
        return hour.toString();
    }

    private String createInvalidHour(){
        int typeOfInvalidHour = rand.nextInt(2);
        if(typeOfInvalidHour == 0){//Let's make a valid hour and then
            //make it invalid
            StringBuilder hour = new StringBuilder(createValidHour());
            int appendingLimit = rand.nextInt(100);
            ++appendingLimit;
            for(int i = 0; i < appendingLimit; ++i){
                hour.append((char)(rand.nextInt('z'-'0') + '0'));
            }
            hour.append('!');

            return hour.toString();
        }
        else{//let's build an invalid string from the ground-up
            StringBuilder hour = new StringBuilder();
            int appendingLimit = rand.nextInt(100);
            ++appendingLimit;
            for(int i = 0; i < appendingLimit; ++i){
                hour.append((char)(rand.nextInt('z'-'0') + '0'));
            }
            hour.append('!');
            return hour.toString();
        }
    }

    private String createHour(int typeOfHour){
        if(typeOfHour != 1){//Test with the valid beg hour
            return createValidHour();
        }
        else{//Test with the invalid beg hour
            return createInvalidHour();
        }
    }

    //Returns a number with 3 or 4 digits, which is easy to work with later
    private int parseHoursToInt(String hours){
        int result = 0;
        int iter = 0;
        if(hours.length() == 4){
            result += (hours.charAt(iter) - '0');
            ++iter;
        }
        else if(hours.length() == 5){
            result += (hours.charAt(iter) - '0');
            result *= 10;
            ++iter;
            result += (hours.charAt(iter) - '0');
            ++iter;
        }
        else{return -1;}//invalid hour
        ++iter;
        result *= 10;
        result += (hours.charAt(iter) - '0');
        ++iter;
        result *= 10;
        result += (hours.charAt(iter) - '0');

        return result;
    }

    private void cleanUp(){
        for(String s : generatedRegistrationsForToday){
            generatedPayments.remove(s);
        }
        generatedRegistrationsForToday.clear();
        for(String s : generatedRegistrationsForTomorrow){
            String h = s;//String's immutability will guarantee that
            //there won't be any problems with shallow copies
            generatedRegistrationsForToday.add(h);
        }
        generatedRegistrationsForTomorrow.clear();
    }

    private void generatePaymentTest(int lineNumber){
        //1. Generate Registration
        int typeOfRegistration = rand.nextInt(10);
        StringBuilder test = new StringBuilder();
        String registration = createRegistration(typeOfRegistration);
        test.append(registration);
        test.append(" ");

        //2. Generate beg and end hours
        int typeOfBegHour = rand.nextInt(10);
        String begHour = createHour(typeOfBegHour);
        test.append(begHour);
        test.append(" ");

        int typeOfEndHour = rand.nextInt(10);
        String endHour = createHour(typeOfEndHour);
        test.append(endHour);

        //Test can (but doesn't have to) end with " ",
        // and it shouldn't have any impact on the result
        if(rand.nextInt(2) == 1){test.append(" ");}

        //3. add to the respective data structures
        int beg = parseHoursToInt(begHour);
        int end = parseHoursToInt(endHour);
        if(beg == -1 || end == -1){
            textToWriteOutFile.add("ERROR " + lineNumber);
        }
        else if(typeOfBegHour != 1 && typeOfEndHour != 1 && typeOfRegistration != 1){
            if(beg >= 800 && beg <= 2000 && end >= 800 &&  end <= 2000){
                if(beg < end && end - beg >= 10){
                    if(beg <= prevTime){
                        cleanUp();//Updating today's day
                    }
                    generatedRegistrationsForToday.add(registration);
                    generatedPayments.put(registration, new String[]{begHour, endHour});
                    textToWriteOutFile.add("OK " + lineNumber);
                    prevTime = beg;
                }
                else if(beg > end){
                    if(beg <= prevTime){
                        cleanUp();//Updating today's day
                    }
                    generatedPayments.put(registration, new String[]{begHour, endHour});
                    generatedRegistrationsForTomorrow.add(registration);
                    textToWriteOutFile.add("OK " + lineNumber);
                    prevTime = beg;
                }
                else{
                    textToWriteOutFile.add("ERROR " + lineNumber);
                }
            }
            else{
                textToWriteOutFile.add("ERROR " + lineNumber);
            }
        }
        else{
            textToWriteOutFile.add("ERROR " + lineNumber);
        }
        textToWriteInFile.add(registration + " " + begHour + " " + endHour);
    }
    private void generateValidationTest(int lineNumber){
        //1. Generate registration
        int typeOfRegistration = rand.nextInt(10);
        StringBuilder test = new StringBuilder();
        String registration = "";
        int dayToUse = rand.nextInt(2);
        if(typeOfRegistration != 1){
            registration = useExistingRegistration(dayToUse);
        }
        else{
            registration = createRegistration(typeOfRegistration);
        }
        if(registration.equals("")){
            typeOfRegistration = 1;
            registration = createRegistration(typeOfRegistration);
        }
        test.append(registration);
        test.append(" ");
        //2. Generate current time
        int typeOfCurrentHour = rand.nextInt(10);
        String currentHour = createHour(typeOfCurrentHour);
        test.append(currentHour);
        //3. Check whether we are still in "today" or not
        //and print results accordingly
        int generatedTime = parseHoursToInt(currentHour);
        if(generatedTime < 800 || generatedTime > 2000){textToWriteOutFile.add("ERROR " + lineNumber);}
        else if(typeOfRegistration != 1 && typeOfCurrentHour != 1){//valid strings were created
            int beg = parseHoursToInt(generatedPayments.get(registration)[0]);
            int end = parseHoursToInt(generatedPayments.get(registration)[1]);
            if(beg < end){//today
                if(generatedTime <= prevTime){
                    cleanUp();//Updating today's day
                }
                if(generatedTime >= beg && generatedTime <= end){
                    textToWriteOutFile.add("YES " + lineNumber);
                }
                else{
                    textToWriteOutFile.add("NO " + lineNumber);
                }
            }
            else{//tomorrow
                if(generatedTime <= prevTime){
                    cleanUp();//Updating today's day
                }
                if((generatedTime >= beg) || (generatedTime <= end)){
                    textToWriteOutFile.add("YES " + lineNumber);
                    //in case of valid tomorrow, we have to fill today's array with tomorrow's, and
                    //then clean the tomorrow's array. Also, it would be nice to free some space
                    //in the generatedPayments map.
                    cleanUp();
                }
                else{
                    textToWriteOutFile.add("NO " + lineNumber);
                }
            }
            prevTime = beg;
        }
        else{//invalid strings were created
            textToWriteOutFile.add("ERROR " + lineNumber);
        }
        textToWriteInFile.add(registration + " " + currentHour);
    }

    public void writeToFiles(){
        try{
            Files.write(inPath, textToWriteInFile, StandardCharsets.UTF_8);
        }
        catch (Exception e){
            System.out.println("Failed to write to the specified inPath.");
        }

        try{
            Files.write(outPath, textToWriteOutFile, StandardCharsets.UTF_8);
        }
        catch (Exception e){
            System.out.println("Failed to write to the specified outPath.");
        }
    }

    public void generateTest(int lineNumber){
        if(!createPaths()){return;}
        if(generatedPayments.isEmpty()){//we have no cars to test
            generatePaymentTest(lineNumber);
        }
        else{//we have cars to test
            int typeOfTestToGenerate = rand.nextInt(10);
            ++typeOfTestToGenerate;
            if(typeOfTestToGenerate%2 == 0){
                generatePaymentTest(lineNumber);
            }
            else{
                generateValidationTest(lineNumber);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        ParkingTestGenerator ptg = new ParkingTestGenerator();
        for(int i = 1; i <= 100; ++i){
            ptg.generateTest(i);
        }
        ptg.writeToFiles();
    }
}
