import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.List;
import java.util.Arrays;
import java.util.stream.*;
import java.util.Collections;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;


public class Circlehistogram extends JPanel {
	private static final long serialVersionUID = 1L;
	private Graphics2D offlineGraphics;
	BufferedImage bimage;
	int[] totalOutcomes = new int[24];  // Signals (24-length array of ints)
	int[] totalRate = new int[24];  // Rates
	int[] totalAccel = new int[24];  // Accelerations
	
	int[] outcomeLabels = new int[24];  // Signal labels
	int[] rateLabels = new int[24];  // Rate labels
	int[] accelLabels = new int[24];  // Acceleration labels
	String[] plotLabels = new String[3]; // Labels that go under plots
	String dataName; // Name of the data
	static int totalReps = 100;

	private void init() {
		// int reps;  // Unused
		double scale;
		bimage = new BufferedImage(7200, 3200, BufferedImage.TYPE_INT_RGB);  // Create image on screen
		offlineGraphics = (Graphics2D)bimage.getGraphics();
		offlineGraphics.setColor(Color.white);  // White background
		offlineGraphics.fillRect(0,0,7200,3200);  // Fill white rectangle
		
		
		int outRadius = 900;  // Radius of each outer circle
		int yMargin = 200; // y margin above circles
		int xMargin = 150; // x margin on left of circles, 2 * x margin between circles
		int xLeftMargin = 0;  // Margin of graphs to left of pane
		
		int yPos = outRadius + yMargin;
		int xPos1 = xLeftMargin + xMargin + outRadius;
		int xPos2 = xLeftMargin + 3 * xMargin + 3 * outRadius;
		int xPos3 = xLeftMargin + 5 * xMargin + 5 * outRadius;
		
		int outerThick = 11;
		int latticeThick = 5;
		int dataThick = 14;
		
		// Draw three circular lattices. Input: centerX, centerY, radius
		drawLattice(xPos1, yPos, outRadius, outerThick, latticeThick);
		drawLattice(xPos2, yPos, outRadius, outerThick, latticeThick);
		drawLattice(xPos3, yPos, outRadius, outerThick, latticeThick);
		
		int outcomeSum = IntStream.of(totalOutcomes).sum();
		int rateSum = IntStream.of(totalRate).sum();
		int accelSum = IntStream.of(totalAccel).sum();
		
		// Get mean of each
		double outcomeMean = outcomeSum/24;
		double rateMean = rateSum/24;
		double accelMean = accelSum/24;
		
		// Get max of each
		double outcomeMax = Arrays.stream(totalOutcomes).max().getAsInt();
		double rateMax = Arrays.stream(totalRate).max().getAsInt();
		double accelMax = Arrays.stream(totalAccel).max().getAsInt();
		
		offlineGraphics.setStroke(new BasicStroke(7));  // Set line width
		
		// Copy the means 24 times
		double[] outcomeMeans = new double[24];
		Arrays.fill(outcomeMeans, outcomeMean);
		double[] rateMeans = new double[24];
		double[] accelMeans = new double[24];
		boolean separate = true;
		if(separate){
			Arrays.fill(rateMeans, rateMean);
			Arrays.fill(accelMeans, accelMean);
		} else {
			Arrays.fill(rateMeans, outcomeMean);
			Arrays.fill(accelMeans, outcomeMean);
		}
		
		//To double
		double[] totalOutcomesD = Arrays.stream(totalOutcomes).asDoubleStream().toArray();
		double[] totalRateD = Arrays.stream(totalRate).asDoubleStream().toArray();
		double[] totalAccelD = Arrays.stream(totalAccel).asDoubleStream().toArray();
		
		// Draw data. drawData takes first a 24-elements array of ints, then the X center, the Y center, the colour, and a `scale'
		scale = 0.98;  // Multiply distance to edge of circle by this amount
		int fontSize = 90;
		int labelFontSize = 120; // Font size of labels under each histogram
		double labelOffset = 80;
		int titleOffset = 200;
		drawData(outcomeMeans, xPos1, yPos,Color.blue,scale,outcomeMax,outRadius,dataThick);  
		drawData(totalOutcomesD, xPos1, yPos,Color.red,scale,outcomeMax,outRadius,dataThick);  
		drawLabels(outcomeLabels, xPos1, yPos, Color.black, outRadius, fontSize, labelOffset);

		drawData(rateMeans, xPos2, yPos,Color.blue,scale,rateMax,outRadius,dataThick);  
		drawData(totalRateD, xPos2, yPos,Color.red,scale,rateMax,outRadius,dataThick);  
		drawLabels(rateLabels, xPos2, yPos, Color.black, outRadius, fontSize, labelOffset);

		drawData(accelMeans, xPos3, yPos,Color.blue,scale,accelMax,outRadius,dataThick);  
		drawData(totalAccelD, xPos3, yPos,Color.red,scale,accelMax,outRadius,dataThick); 
		drawLabels(accelLabels, xPos3, yPos, Color.black, outRadius, fontSize, labelOffset);
				
		// Draw plot labels
		drawPlotLabel(plotLabels[0],xPos1,yPos + (int)labelOffset + titleOffset,outRadius,labelFontSize,Color.black);
		drawPlotLabel(plotLabels[1],xPos2,yPos + (int)labelOffset + titleOffset,outRadius,labelFontSize,Color.black);
		drawPlotLabel(plotLabels[2],xPos3,yPos + (int)labelOffset + titleOffset,outRadius,labelFontSize,Color.black);
		
		// Save image
		String fileName = "ratePlots";
		save(bimage, fileName);
		
	}
	
	public void drawLattice(int centerX, int centerY, int radius, int outerThick, int latticeThick) {
		int i;

		offlineGraphics.setStroke(new BasicStroke(outerThick));
		offlineGraphics.setColor(Color.black);
		offlineGraphics.drawOval(centerX - radius, centerY - radius, radius*2, radius*2);
		offlineGraphics.setStroke(new BasicStroke(latticeThick));
		
		// Draw smaller circles inside lattice
		for (i = 1; i < 6; ++i) {
			offlineGraphics.drawOval(centerX - radius*i/6, centerY - radius*i/6, i*radius/3, i*radius/3);
		}
		
		//Draw 12 lines from center to edges
		for (i = 0; i < 12; ++i) {
			offlineGraphics.drawLine(centerX-(int)(radius*Math.sin(i*Math.PI/12)),centerY-(int)(radius*Math.cos(i*Math.PI/12)),centerX+(int)(radius*Math.sin(i*Math.PI/12)), centerY+(int)(radius*Math.cos(i*Math.PI/12)));
		}
	}

	public void paintComponent(Graphics g) {
		 super.paintComponent(g);
		 g.drawImage(bimage,0,0,1800,800,0,0,7200,3200,null);
	}
	
	public void drawData(double[] data, int centerX, int centerY, Color color, double scale, double dataMax, int outRadius, int dataThick) {
		int i;
		int[] x,y;
		x = new int[data.length];
		y = new int[data.length];
		offlineGraphics.setStroke(new BasicStroke(dataThick));
		offlineGraphics.setColor(color);
		for (i = 0; i < data.length; ++i) {
			x[i] = centerX + (int)(Math.sin(i*Math.PI/12)*outRadius*(data[i]/dataMax)*scale); 
			y[i] = centerY - (int)(Math.cos(i*Math.PI/12)*outRadius*(data[i]/dataMax)*scale); 
		}
		for (i = 0; i < data.length; ++i) {
			offlineGraphics.drawLine(x[i],y[i],x[(i+1)%data.length],y[(i+1)%data.length]);
		}
		
	}
	
	public void drawLabels(int[] labels, int centerX, int centerY, Color color, int outRadius, int fontSize, double labelOffset) {
		offlineGraphics.setColor(color);
		Font font = new Font("ComputerModern", Font.PLAIN, fontSize);
		offlineGraphics.setFont(font); 
		FontMetrics metrics = offlineGraphics.getFontMetrics(font);
		for (int i = 0; i < labels.length; ++i) {
			String lab = String.valueOf(labels[i]);
			int xOff;
			xOff = metrics.stringWidth(lab)/2;
			int yOff;
			yOff = (metrics.getHeight()/2) - metrics.getAscent();
			offlineGraphics.drawString(String.valueOf(labels[i]),
									   centerX-xOff+(int)(Math.sin(i*2*Math.PI/labels.length)*(outRadius+labelOffset)),
									   centerY-yOff+(int)(Math.cos(i*2*Math.PI/labels.length)*(outRadius+labelOffset)));
		}
	}
	
	public void drawPlotLabel(String plotLabel, int centerX, int centerY, int outRadius, int fontSize, Color color) {
		offlineGraphics.setColor(color);
		Font font = new Font("ComputerModern", Font.PLAIN, fontSize);
		offlineGraphics.setFont(font);
		FontMetrics metrics = offlineGraphics.getFontMetrics(font);
		List<String> lines = Arrays.asList(plotLabel.split("\\s*@\\s*"));
		int yExtra = 0;
		for(String curLine : lines){
			int xOff = metrics.stringWidth(curLine)/2;
			int yOff = (metrics.getHeight()/2) - metrics.getAscent();
			offlineGraphics.drawString(curLine, centerX - xOff, centerY + outRadius - yOff + yExtra);  
			yExtra += metrics.getHeight();
		}
		
	}
	
	// Save image as .png file
	public void save(BufferedImage bimage, String saveName)
	{
		try {
				if (ImageIO.write(bimage, "png", new File("./" + dataName.replace(".", "_dot_") + "_rateplots.png")))
				{
					System.out.println("Image saved as " + dataName.replace(".", "_dot_") + "_rateplots.png");
				}
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("ModN viewer");  // Make main window
		Circlehistogram viewer;  // Create viewer object
		viewer = new Circlehistogram();  // Initialize viewer object
		if (args.length > 0) {  // If arguments have been passed to viewer from terminal
			// StringBuilder resultStringBuilder = new StringBuilder();  // Unused
			try {
				viewer.dataName = args[0];
				BufferedReader br = new BufferedReader(new FileReader(viewer.dataName));  // Read file that has been passed as argument
				String[] line = br.readLine().split(",");
				for (int i = 0; i < 24; ++i) {
					viewer.totalOutcomes[i] = Integer.parseInt(line[i].strip());
				}
				line = br.readLine().split(",");
				for (int i = 0; i < 24; ++i) {
					viewer.totalRate[i] = Integer.parseInt(line[i].strip());
				}
				line = br.readLine().split(",");
				for (int i = 0; i < 24; ++i) {
					viewer.totalAccel[i] = Integer.parseInt(line[i].strip());
				}
				line = br.readLine().split(",");
				for (int i = 0; i < 24; ++i) {
					viewer.outcomeLabels[i] = Integer.parseInt(line[i].strip());
				}
				line = br.readLine().split(",");
				for (int i = 0; i < 24; ++i) {
					viewer.rateLabels[i] = Integer.parseInt(line[i].strip());
				}
				line = br.readLine().split(",");
				for (int i = 0; i < 24; ++i) {
					viewer.accelLabels[i] = Integer.parseInt(line[i].strip());
				}
				line = br.readLine().split(",");
				for (int i = 0; i < 3; ++i) {
					viewer.plotLabels[i] = line[i].strip();
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Please enter a .csv file as argument.");
		}
		viewer.init();


		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(viewer);
		frame.setSize(1800,800);
		frame.setVisible(true);

	}
	
	


	
}
