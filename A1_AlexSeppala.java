
//CMPE212 assignment 1
//This program calculates the position over time of a firework projectile using Newtonian and Non-Newtonian physics
//and outputs the (x,y) coordinates it to a .txt file in the same workspace
//By Alex Seppala
//13as191
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.IOException;

public class A1_13as191 {
	public static void main(String[] args) {
		System.out.println(
				"This program calculates the position over time of a firework projectile using Newtonian and Non-Newtonian physics");
		System.out.println("An angle and wind velocity must be inputted by the user");
		System.out.println("The output will be saved to a file");
		double value;
		double temp[] = Input();
		double Angle = temp[0];
		double VWindKmH = temp[1];
		double VWind = VWindKmH * 5 / 18; // converts to m/s
		final double VLAUNCH = 22; // m/s
		int i, j;
		final double INITIALWEIGHT = 0.008; // kg
		final double BURN_RATE = 0.0030; // kg / second
		final double DELTA_T = 0.05; // seconds
		double TmaxI = (INITIALWEIGHT / BURN_RATE) / DELTA_T;
		int TmaxInt = (int) TmaxI;
		double[][] NewtPosition = new double[60][2];
		double[] RKVx = new double[60];
		double[] RKVy = new double[60];
		double[][] NonNewtPosition = new double[60][2];
		NonNewtPosition[0][0] = 0;
		NonNewtPosition[0][1] = 0;

		// This nested for loop stores the Newtonian positions into a 2D array
		for (j = 0; j <= 1; j++) {
			for (i = 0; i <= TmaxInt; i++) {
				if (j == 0) { // x dimension
					value = XMotion(0, VxComponent(VLAUNCH, Angle), i);
					NewtPosition[i][j] = value;
				} else if (j == 1) { // y dimension
					value = YMotion(0, VyComponent(VLAUNCH, Angle), i);
					NewtPosition[i][j] = value;
				}
			}
		}
		String OutputPhrase = ("Results with a launch angle of " + String.valueOf(Angle)
				+ " degrees and a wind speed of " + String.valueOf(VWindKmH) + " Km/h:\r\n\r\n");
		OutputPhrase += "Newtonian Trajectory (x,y):\r\n";
		for (i = 0; i <= TmaxInt; i++) {
			double iDouble = i;
			double time = iDouble / 20;
			OutputPhrase += "At time " + String.format("%.2f", time) + " Seconds: "
					+ String.format("%.2f", NewtPosition[i][0]) + ", " + String.format("%.2f", NewtPosition[i][1])
					+ "\r\n";
		}
		// This creates an array of velocities
		for (i = 0; i <= TmaxInt; i++) {
			if (i == 0) {
				double VxInitial = VxComponent(VLAUNCH, Angle);
				double Vxa = Vxa(VxInitial, VWind);
				RKVx[i] = Vxa;
				RKVy[i] = VyComponent(VLAUNCH, Angle);
			} else {
				double store[] = RungeKutta(i, RKVx[i - 1], RKVy[i - 1]);
				RKVx[i] = store[0];
				RKVy[i] = store[1];
			}
		}
		// This loop fills the Non Newtonian Position array
		for (j = 0; j <= 1; j++) {
			for (i = 1; i <= TmaxInt + 1; i++) {
				int iminus1 = i - 1;
				if (j == 0) {
					NonNewtPosition[i][j] = NonNewtPosition[iminus1][j] + (RKVx[i] * DELTA_T);
				} else if (j == 1) {
					NonNewtPosition[i][j] = NonNewtPosition[iminus1][j] + (RKVy[i] * DELTA_T);
				}
			}
		}

		OutputPhrase += "\r\nNon-Newtonian Trajectory (x,y):\r\n";
		for (i = 0; i <= TmaxInt; i++) {
			double iDouble = i;
			double time = iDouble / 20;
			OutputPhrase += "At time " + String.format("%.2f", time) + " Seconds: "
					+ String.format("%.2f", NonNewtPosition[i][0]) + ", " + String.format("%.2f", NonNewtPosition[i][1])
					+ "\r\n";
		}
		System.out.println("Complete! Look in your project workspace for the file called Assn1_Output_13as191.");
		writeTextPhrase("Assn1_Output_13as191", OutputPhrase);
	}

	// The Input method prompts the user to input the angle and wind velocity
	// using the scanner.
	public static double[] Input() {
		Scanner screen = new Scanner(System.in);
		double Angle, VWindKmH;
		do {
			System.out.println("Please type the launch angle in degrees (between -15 and 15 degrees from vertical): ");
			Angle = screen.nextDouble();
		} while (!ValidAngle(Angle));
		System.out.println("Angle input success");
		do {
			System.out.println(
					"Please type the wind velocity (between -20 and 20 Km/h) with positive being the forward direction: ");
			VWindKmH = screen.nextDouble();
		} while (!ValidVWindKmH(VWindKmH));

		System.out.println("Wind Velocity input success");
		return new double[] { Angle, VWindKmH };
	}

	// Determines whether the angle is within the required boundaries and loops
	// again
	public static boolean ValidAngle(double Angle) {
		if (Angle < -15 || Angle > 15) {
			System.out.println("Out of bounds! Try again");
			return false;
		} else {
			return true;
		}
	}

	// Determines whether the wind velocity is within the required boundaries
	// and loops again
	public static boolean ValidVWindKmH(double VWindKmH) {
		if (VWindKmH < -20 || VWindKmH > 20) {
			System.out.println("Out of bounds! Try again");
			return false;
		} else {
			return true;
		}
	}

	// This method converts the input angle to radians and calculates the x
	// component of the input velocity
	public static double VxComponent(double V, double Angle) {
		double AngleRad = Math.toRadians(Angle);
		double Vx0 = V * (Math.sin(AngleRad));
		return Vx0;
	}

	// This method converts the input angle to radians and calculates the y
	// component of the input velocity
	public static double VyComponent(double VLAUNCH, double Angle) {
		double AngleRad = Math.toRadians(Angle);
		double Vy0 = VLAUNCH * (Math.cos(AngleRad));
		return Vy0;
	}

	// This method calculates the Newtonian x position
	public static double XMotion(double X0, double VxComponent, double i) {
		double t = i / 20; // This converts the i into increments of 0.05
		double XMotion = X0 + (VxComponent * t);
		return XMotion;
	}

	// This method calculates the Newtonian y position
	public static double YMotion(double Y0, double VyComponent, double i) {
		double t = i / 20;// This converts the i into increments of 0.05
		double g = 9.807;// m/s^2
		double YMotion = Y0 + VyComponent * t - (0.5) * g * (Math.pow(t, 2));
		return YMotion;
	}

	// This adjusts the horizontal velocity to incorporate wind
	public static double Vxa(double Vx, double Vwind) {
		double Vxa = Vx + Vwind;
		return Vxa;
	}

	// This calculates the magnitude of a velocity taking its x and y components
	public static double V(double Vxa, double Vy) {
		double V = Math.sqrt(((Math.pow(Vxa, 2) + Math.pow(Vy, 2))));
		return V;
	}

	// Runge-Kutta equation for calculating the non-Newtonian velocity at any
	// given time
	public static double[] RungeKutta(double i, double Vxa, double Vy) {
		double q1x = DVxDt(i, Vxa, Vy);
		double q1y = DVyDt(i, Vxa, Vy);
		double q2x = DVxDt(i + 0.025, Vxa + 0.025 * q1x, Vy + 0.025 * q1y);
		double q2y = DVyDt(i + 0.025, Vxa + 0.025 * q1x, Vy + 0.025 * q1y);
		double q3x = DVxDt(i + 0.025, Vxa + 0.025 * q2x, Vy + 0.025 * q2y);
		double q3y = DVyDt(i + 0.025, Vxa + 0.025 * q2x, Vy + 0.025 * q2y);
		double q4x = DVxDt(i + 0.05, Vxa + 0.05 * q3x, Vy + 0.05 * q3y);
		double q4y = DVyDt(i + 0.05, Vxa + 0.05 * q3x, Vy + 0.05 * q3y);
		double VxRK = Vxa + (0.05 / 6) * (q1x + 2 * q2x + 2 * q3x + q4x);
		double VyRK = Vy + (0.05 / 6) * (q1y + 2 * q2y + 2 * q3y + q4y);
		return new double[] { VxRK, VyRK };
	}

	// The equation for the derivative of the x component of the velocity
	public static double DVxDt(double i, double Vxa, double Vy) {
		double FDrag = FDrag(Vxa, Vy, i);
		double Mass = Mass(i);
		double V = V(Vxa, Vy);
		double VxWrtTime = -(FDrag * Vxa) / (Mass * V);
		return VxWrtTime;
	}

	// The equation for the derivative of the y component of the velocity
	public static double DVyDt(double i, double Vxa, double Vy) {
		double g = 9.807; // m/s^2
		double FDrag = FDrag(Vxa, Vy, i);
		double Mass = Mass(i);
		double V = V(Vxa, Vy);
		double VyWrtTime = -g - (FDrag * Vy) / (Mass * V);
		return VyWrtTime;
	}

	// This determines what the mass is at any given time
	public static double Mass(double i) {
		double t = i / 20; // converts i value to seconds
		double INITIALWEIGHT = 0.008; // kg
		double BURN_RATE = 0.0030; // kg / second
		double Mass = INITIALWEIGHT - BURN_RATE * t;
		if ((BURN_RATE * t) >= INITIALWEIGHT) { // to prevent a negative mass
												// from occuring
			Mass = 0;
		}
		return Mass;
	}

	// This calculates the area of the projectile
	public static double A(double i) {
		double Mass = Mass(i);
		double Density = 1900; // kg/m^3
		double Volume = Mass / Density; // m^3
		double Radius = Math.cbrt((3 * Volume) / (4 * Math.PI)); // m
		double A = (3 * Volume) / (4 * Radius); // m^2
		return A;
	}

	// This calculates what the drag force is
	public static double FDrag(double Vxa, double Vy, double i) {
		double V = V(Vxa, Vy);
		double A = A(i);
		double p = 1.2;
		double Cd = 0.4;
		double FDrag = (p * Math.pow(V, 2) * A * Cd) / 2;
		return FDrag;
	}

	// This method writes a phrase in the output file and then continues to the
	// next line
	public static void writeTextPhrase(String outputFile, String Phrase) {
		Path file = Paths.get(outputFile);
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			writer.write(Phrase + "\r\n");
		} catch (IOException err) {
			System.err.println(err.getMessage());
		}
	}
}