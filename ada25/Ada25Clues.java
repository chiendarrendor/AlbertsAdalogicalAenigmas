

public class Ada25Clues
{
	public static Constraint[] clues = new ClueConstraint[]
	{
		new ClueConstraint() { public void nums() { go("1A","24A","1D"); }         public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("3A","28A"); }              public boolean isFulfilled(int... v) { return dividesExactly(v[0],v[1],3); }},
		new ClueConstraint() { public void nums() { go("6A","14A","31D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] * v[2]; }},
		new ClueConstraint() { public void nums() { go("8A","20D","31D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] * v[2]; }},
		new ClueConstraint() { public void nums() { go("10A","14A"); }             public boolean isFulfilled(int... v) { return v[0] == v[1] + 3; }},
		new ClueConstraint() { public void nums() { go("12A","9D","28A"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("14A","29D","20D"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("15A","14A","19D"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("17A","1D"); }              public boolean isFulfilled(int... v) { return v[0] == v[1] - 3; }},
		new ClueConstraint() { public void nums() { go("19A","16D","8A"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("21A","26A","32A"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("23A","12A","5D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("24A","3D","23A"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("26A","11D","22D"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("28A","5D","17A"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("30A","3A","20D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("32A","33A","3D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("33A","11D","13D","28A");}  public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2] + v[3]; }},
		
		new ClueConstraint() { public void nums() { go("1D","10A","11D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("2D","13D"); }              public boolean isFulfilled(int... v) { return v[0] == v[1] - 2; }},
		new ClueConstraint() { public void nums() { go("3D","33A"); }              public boolean isFulfilled(int... v) { return dividesExactly(v[0],v[1],3); }},
		new ClueConstraint() { public void nums() { go("4D","11D","31D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("5D","2D","22D"); }         public boolean isFulfilled(int... v) { return v[0] == v[1] * v[2]; }},
		new ClueConstraint() { public void nums() { go("7D","15A"); }              public boolean isFulfilled(int... v) { return v[0] == v[1] * 6; }},
		new ClueConstraint() { public void nums() { go("9D","17A","23A"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("11D","2D","20D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("13D","18D","17A"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("16D","33A","31D"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("18D","19A","2D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("19D","4D","10A"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("20D","22D"); }             public boolean isFulfilled(int... v) { return v[0] == v[1] - 6; }},
		new ClueConstraint() { public void nums() { go("21D","8A","10A"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("22D","19D","14A"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] - v[2]; }},
		new ClueConstraint() { public void nums() { go("23D","4D","31D"); }        public boolean isFulfilled(int... v) { return v[0] == v[1] * v[2]; }},
		new ClueConstraint() { public void nums() { go("25D","21A","30A"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("27D","24A","25D"); }       public boolean isFulfilled(int... v) { return v[0] == v[1] + v[2]; }},
		new ClueConstraint() { public void nums() { go("29D","26A"); }             public boolean isFulfilled(int... v) { return v[0] == v[1] + 5; }},
		new ClueConstraint() { public void nums() { go("31D","20D"); }             public boolean isFulfilled(int... v) { return dividesExactly(v[0],v[1],2); }}
	};
}

