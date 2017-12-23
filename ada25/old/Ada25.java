
public class Ada25
{

	// this gets the nth digit of num as a base 10 number, where the 1's place is 0, the 10s place is 1, etc.
	static int getDigit(int num,int digit)
	{
		while(digit > 0) { num /= 10 ; --digit; }
		return num % 10;
	}


	public static void main(String[] args)
	{

		// invariants:
		// 1D -- 3 digits
		// 10A -- 2 digits
		// 11D -- 2 digits
		//
		// A
		// B
		// CD
		//  E
		//       -- ABC - 1D
		//       -- CD - 10A
		//       -- DE - 11D
		//
		//  1D - 10A - 11D = 0
		
		for (int oned = 100 ; oned < 1000 ; ++oned)
		{
			for (int tena = 10 ; tena < 100 ; ++tena)
			{
				if (getDigit(oned,0) != getDigit(tena,1)) continue;

				for (int elevend = 10 ; elevend < 100 ; ++elevend)
				{
					if (getDigit(tena,0) != getDigit(elevend,1)) continue;
					if (oned - tena - elevend != 0) continue;
					System.out.println("1D: " + oned + " 10A: " + tena + " 11D: " + elevend);
				}
			}
		} 
	}
}