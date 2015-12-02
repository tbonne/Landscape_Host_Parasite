package LHP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

/**
 * Class for reading PGM files into a 2D array.
 *
 * @author Eric Tatara
 * @author Nick Collier
 * @version 
 */

public class PGMReader {

	protected double matrix[][];
	protected int xSize;
	protected int ySize;
	protected long topLeftX,topLeftY;
	protected int cellSize;
	protected double noDataValue;
	
	public PGMReader(String sugarFile) {
		
		java.io.InputStream stream = null;
		try {
			File file1 = new File(sugarFile);
			//System.out.println("file exists "+ file1.exists());
		    
			stream = new FileInputStream(file1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

		init(in);
	}

	private void init(BufferedReader in) {
		try {
			StringTokenizer tok;

			String str = in.readLine();

			if (!str.equals("P2")) {
				throw new UnsupportedEncodingException("File is not in PGM ascii format");
			}

			str = in.readLine();
			tok = new StringTokenizer(str);
			xSize = Integer.valueOf(tok.nextToken()).intValue();
			ySize = Integer.valueOf(tok.nextToken()).intValue();
			topLeftX=Integer.valueOf(tok.nextToken()).intValue(); //this should be the top left
			topLeftY=Integer.valueOf(tok.nextToken()).intValue();//this should be the top left
			cellSize=Integer.valueOf(tok.nextToken()).intValue();
			
			matrix = new double[ySize][xSize]; //this assumes y is the number of rows and x is the number of columns

			in.readLine();

			str = "";
			String line = in.readLine();

			while (line != null) {
				str += line + " ";
				line = in.readLine();
			}
			in.close();

			tok = new StringTokenizer(str);

			for (int i = 0; i < ySize; i++)
				for (int j = 0; j < xSize; j++) 
				 
					matrix[i][j] = Double.valueOf(tok.nextToken());

		} catch (IOException ex) {
			System.out.println("Error Reading image file");
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public double[][] getMatrix() {
		return matrix;
	}
	public double getNoDataValue(){
		return noDataValue;
	}
	public long getTopLeftX(){
		return topLeftX;
	}
	public long getTopLeftY(){
		return topLeftY;
	}
	public int getCellSize(){
		return cellSize;
	}
	public int getRows(){
		return ySize;
	}
	public int getCols(){
		return xSize;
	}
}
