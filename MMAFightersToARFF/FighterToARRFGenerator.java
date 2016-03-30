import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class FighterToARRFGenerator 
{
	public final static double FEET_TO_CENTIMETER = 30.48;
	public final static double INCHES_TO_CENTIMETER = 2.54;
	public static Set<String> fighterNames = new TreeSet<String>();
	
	public static String getFighterStats(DBCursor cursor, Map<String,String> fs)
	{
		StringBuilder fighterData = new StringBuilder("");
		
		//Extract fighter name to be used as the key into the map to retrieve his stats
		String name = (String)cursor.one().get("name");
		
		//Get the rest of the stats
		String tempWeight = (String)cursor.one().get("weight");
		String weight = tempWeight.replaceAll(" lbs", "");
		fighterData.append(weight + ",");
		String weight_class = (String)cursor.one().get("weight_class");
		fighterData.append(weight_class.toLowerCase() + ",");
		fighterData.append((String)cursor.one().get("age") + ",");
		String tempHeight = (String)cursor.one().get("height");
		String height = String.valueOf(convertFeetAndInchestoCentimeter(tempHeight));
		fighterData.append(height + ",");
		fighterData.append((String)cursor.one().get("wins_total") + ",");
		fighterData.append((String)cursor.one().get("wins_ko") + ",");
		fighterData.append((String)cursor.one().get("wins_to") + ",");
		fighterData.append((String)cursor.one().get("wins_decision") + ",");
		fighterData.append((String)cursor.one().get("wins_other") + ",");
		fighterData.append((String)cursor.one().get("losses_total") + ",");
		fighterData.append((String)cursor.one().get("losses_ko") + ",");
		fighterData.append((String)cursor.one().get("losses_to") + ",");
		fighterData.append((String)cursor.one().get("losses_decision") + ",");
		fighterData.append((String)cursor.one().get("losses_other") + ",");
		fighterData.append((String)cursor.one().get("strikes_attempted") + ",");
		fighterData.append((String)cursor.one().get("strikes_successful") + ",");
		fighterData.append((String)cursor.one().get("strikes_standing") + ",");
		fighterData.append((String)cursor.one().get("strikes_clinch") + ",");
		fighterData.append((String)cursor.one().get("strikes_ground") + ",");
		fighterData.append((String)cursor.one().get("takedowns_attempted") + ",");
		fighterData.append((String)cursor.one().get("takedowns_successful") + ",");
		fighterData.append((String)cursor.one().get("takedowns_submissions") + ",");
		fighterData.append((String)cursor.one().get("takedowns_passes") + ",");
		fighterData.append((String)cursor.one().get("takedowns_sweeps"));
		
		//Save fighter data into map to be accessed whenever is it required
		//to avoid getting data again from database		
		fs.put(name, fighterData.toString());
		
		return fighterData.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static String getFighterFights(DBCursor cursor, DBCollection coll, String fighterData, Map<String,String> fs)
	{
		StringBuilder fighterFights = new StringBuilder("");
		
		ArrayList<DBObject> fights = (ArrayList<DBObject>)cursor.one().get("fights");
		
		for(DBObject fight : fights)
		{
			String opponent = (String)fight.get("opponent");
			String result = (String)fight.get("result");
			
			String opponentStats = fs.get(opponent);
			
			if(opponentStats == null)
			{
				if(fighterNames.contains(opponent))
				{
					DBObject query = new BasicDBObject("name", opponent);
					DBCursor opponent_cursor = coll.find(query);
					opponentStats = getFighterStats(opponent_cursor, fs);
				}
				else
					continue;
			}
			
			fighterFights.append(fighterData);
			fighterFights.append(',');
			fighterFights.append(opponentStats);
			fighterFights.append(',');
			
			if(result.equals("win"))
				fighterFights.append("0");
			else
				fighterFights.append("1");
			
			fighterFights.append("\n");
		}
		
		return fighterFights.toString();
	}
	
	public static int convertFeetAndInchestoCentimeter(String feetsInches)
	{
		StringBuilder feet = new StringBuilder("");
		StringBuilder inches = new StringBuilder("");
		
		int index = 0;
		
		while(feetsInches.charAt(index) != '\'')
		{
			feet.append(feetsInches.charAt(index));
			index++;
		}
		
		while(!Character.isDigit(feetsInches.charAt(index)))
			index++;
		
		while(feetsInches.charAt(index) != '\"')
		{
			inches.append(feetsInches.charAt(index));
			index++;
		}
		
		double centimeters = 0;
		
		double f = Integer.parseInt(feet.toString());
		centimeters = Math.round(f * FEET_TO_CENTIMETER);
		
		double i = Integer.parseInt(inches.toString());
		centimeters += Math.round(i * INCHES_TO_CENTIMETER);
		
		return (int)centimeters;
	}
	
	public static void main(String[] args) 
	{	
		//WRITE ARFF HEADER TO OUTPUT STRING---------------------------------------------
		System.out.println("-Writing header file...");
		
		File arffHeader = new File(args[0]);
		String arff = "";
		
		try 
		{
			Scanner scan = new Scanner(arffHeader);
			
			while(scan.hasNextLine())
			{
				arff += scan.nextLine() + "\n";
			}
			
			scan.close();
		} 
		catch (FileNotFoundException e1) 
		{
			System.out.println("Error: Header ARFF text file not found!!!\n");
			e1.printStackTrace();
		}
		
		System.out.println("-Finished Writing header file...");
		//END WRITING ARFF HEADER--------------------------------------------------------
		
		File fighters = new File(args[1]);
		
		try 
		{
			Scanner scan = new Scanner(fighters);
			
			while(scan.hasNextLine())
			{
				FighterToARRFGenerator.fighterNames.add(scan.nextLine());
			}
			
			scan.close();
		} 
		catch (FileNotFoundException e1) 
		{
			e1.printStackTrace();
		}
		
		//BEGIN WRITING FIGHTER DATA FROM DATABASE---------------------------------------
		System.out.println("-Getting fighter data from MongoDB...");
		
		try 
		{
			Map<String,String> fightersStats = new HashMap<String,String>();
			MongoClient mc = new MongoClient("localhost", 27017);
			DB db = mc.getDB("fighters2");
			DBCollection coll = db.getCollection("fighters");
			
			for(String fighter : FighterToARRFGenerator.fighterNames)
			{
				//Get the fighter object by querying his name
				DBObject query = new BasicDBObject("name", fighter);
				DBCursor cursor = coll.find(query);
				
				String fighterData;
				String fighterStats = fightersStats.get((String)cursor.one().get("name"));
				
				if(fighterStats == null)
					fighterData = FighterToARRFGenerator.getFighterStats(cursor, fightersStats);
				else
					fighterData = fighterStats;
				
				String fighterFights = FighterToARRFGenerator.getFighterFights(cursor, coll, fighterData.toString(), fightersStats);
				
				arff += fighterFights;
				
				System.out.println("***********************************************************************************");
				System.out.println("@Got: " + (String)cursor.one().get("name") + "\n" + fighterFights);
				System.out.println("***********************************************************************************");
			}
			
			mc.close();
		} 
		catch (UnknownHostException e2) 
		{
			System.out.println("Error: Could not connect to MongoDB!!!\n");
			e2.printStackTrace();
		}
		
		System.out.println("-Finished getting fighter data from MongoDB...");
		//END WRITING FIGHTER DATA FROM DATABASE-----------------------------------------
		
		//BEGIN WRITING OUTPUT STRING TO OUTPUT FILE-------------------------------------
		
		try 
		{
			PrintWriter outputARFF = new PrintWriter(args[2]);
			outputARFF.print("");
			
			StringBuilder finalOutput = new StringBuilder(arff.replaceAll("NaN", "?"));
			finalOutput = new StringBuilder(finalOutput.toString().replaceAll("N/A", "?"));
			finalOutput = new StringBuilder(finalOutput.toString().replaceAll("n/a", "?"));
			
			outputARFF.print(finalOutput.toString()); 
			outputARFF.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		//END WRITING OUTPUT STRING TO OUTPUT FILE---------------------------------------
	}
}
