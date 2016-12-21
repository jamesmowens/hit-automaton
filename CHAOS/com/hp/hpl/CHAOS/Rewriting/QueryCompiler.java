package com.hp.hpl.CHAOS.Rewriting;

/**
 * The class creates stacks according to the input queries. The current version
 * only supports creating stacks for one query. It needs to be extended for
 * creating stacks for multiple queries.
 * 
 * @author liumo
 * 
 */
public class QueryCompiler {

	// hierarchical pattern graph
	HierarchicalQueryPlan hqp = new HierarchicalQueryPlan();
	// tree including the concept hierarchy
	ConceptTree tree = new ConceptTree();

	public static ConceptTree createTreeCompany() {
		ConceptBound leftCompany = new ConceptBound(1); // create bounds for USA
		ConceptBound rightCompany = new ConceptBound(18);
		ConceptBound[][] CompanyBounds = new ConceptBound[2][1];
		CompanyBounds[0][0] = leftCompany;
		CompanyBounds[1][0] = rightCompany;

		ConceptNode Company = new ConceptNode("COMPANY", 1, CompanyBounds);// create
		// USA
		// node

		ConceptNode[] level1Nodes = new ConceptNode[1];
		level1Nodes[0] = Company;
		ConceptLevel level1 = new ConceptLevel(1, level1Nodes);// create level 1

		ConceptBound leftComputer = new ConceptBound(2); // create bounds for
		// COMPUTER
		ConceptBound rightComputer = new ConceptBound(6);
		ConceptBound[][] ComputerBounds = new ConceptBound[2][1];
		ComputerBounds[0][0] = leftComputer;
		ComputerBounds[1][0] = rightComputer;

		ConceptNode COMPUTER = new ConceptNode("COMPUTER", 2, ComputerBounds);// create
		// COMPUTER
		// node

		ConceptBound leftFinance = new ConceptBound(7); // create bounds for
		// Finance
		ConceptBound rightFinance = new ConceptBound(11);
		ConceptBound[][] FinanceBounds = new ConceptBound[2][1];
		FinanceBounds[0][0] = leftFinance;
		FinanceBounds[1][0] = rightFinance;

		ConceptNode FINANCE = new ConceptNode("FINANCE", 2, FinanceBounds);// create
		// Finance
		// node

		ConceptBound leftEducation = new ConceptBound(12); // create bounds for
		// EDUCATION
		ConceptBound rightEducation = new ConceptBound(17);
		ConceptBound[][] EducationBounds = new ConceptBound[2][1];
		EducationBounds[0][0] = leftEducation;
		EducationBounds[1][0] = rightEducation;

		ConceptNode EDUCATION = new ConceptNode("EDUCATION", 2, EducationBounds);// create
		// Education
		// node

		ConceptNode[] level2Nodes = new ConceptNode[3];
		level2Nodes[0] = COMPUTER;
		level2Nodes[1] = FINANCE;
		level2Nodes[2] = EDUCATION;
		ConceptLevel level2 = new ConceptLevel(2, level2Nodes);// create level 2

		ConceptBound leftINTC = new ConceptBound(3); // create bounds for
		// NTC
		ConceptBound rightINTC = new ConceptBound(3);
		ConceptBound[][] INTCBounds = new ConceptBound[2][1];
		INTCBounds[0][0] = leftINTC;
		INTCBounds[1][0] = rightINTC;

		ConceptNode INTC = new ConceptNode("INTC", 3, INTCBounds);// create
		// INTC
		// node

		ConceptBound leftMSFT = new ConceptBound(4); // create bounds for
		// MSFT
		ConceptBound rightMSFT = new ConceptBound(4);
		ConceptBound[][] MSFTBounds = new ConceptBound[2][1];
		MSFTBounds[0][0] = leftMSFT;
		MSFTBounds[1][0] = rightMSFT;

		ConceptNode MSFT = new ConceptNode("MSFT", 3, MSFTBounds);// create
		// MSFT
		// node

		ConceptBound leftDELL = new ConceptBound(5); // create bounds for
		// Dallas
		ConceptBound rightDELL = new ConceptBound(5);
		ConceptBound[][] DELLBounds = new ConceptBound[2][1];
		DELLBounds[0][0] = leftDELL;
		DELLBounds[1][0] = rightDELL;

		ConceptNode DELL = new ConceptNode("DELL", 3, DELLBounds);// create
		// DELL
		// node

		
		
		
		ConceptBound leftQQQ = new ConceptBound(8); // create bounds for
		// QQQ
		ConceptBound rightQQQ = new ConceptBound(8);
		ConceptBound[][] QQQBounds = new ConceptBound[2][1];
		QQQBounds[0][0] = leftQQQ;
		QQQBounds[1][0] = rightQQQ;

		ConceptNode QQQ = new ConceptNode("QQQ", 3, QQQBounds);// create
		// QQQ
		// node

		ConceptBound leftYHOO = new ConceptBound(9); // create bounds for YHOO
		ConceptBound rightYHOO = new ConceptBound(9);
		ConceptBound[][] YHOOBounds = new ConceptBound[2][1];
		YHOOBounds[0][0] = leftYHOO;
		YHOOBounds[1][0] = rightYHOO;

		ConceptNode YHOO = new ConceptNode("YHOO", 3, YHOOBounds);// create
		// YHOO
		// node

		ConceptBound leftRIMM = new ConceptBound(10); // create bounds for YHOO
		ConceptBound rightRIMM = new ConceptBound(10);
		ConceptBound[][] RIMMBounds = new ConceptBound[2][1];
		RIMMBounds[0][0] = leftRIMM;
		RIMMBounds[1][0] = rightRIMM;

		ConceptNode RIMM = new ConceptNode("RIMM", 3, RIMMBounds);// create
		// RIMM
		// node

		ConceptBound leftIPIX = new ConceptBound(13); // create bounds for
		// IPIX
		ConceptBound rightIPIX = new ConceptBound(13);
		ConceptBound[][] IPIXBounds = new ConceptBound[2][1];
		IPIXBounds[0][0] = leftIPIX;
		IPIXBounds[1][0] = rightIPIX;

		ConceptNode IPIX = new ConceptNode("IPIX", 3, IPIXBounds);// create
		// IPIX
		// node

		ConceptBound leftAMAT = new ConceptBound(14); // create bounds for
		// AMAT
		ConceptBound rightAMAT = new ConceptBound(14);
		ConceptBound[][] AMATBounds = new ConceptBound[2][1];
		AMATBounds[0][0] = leftAMAT;
		AMATBounds[1][0] = rightAMAT;

		ConceptNode AMAT = new ConceptNode("AMAT", 3, AMATBounds);// create
		// AMAT
		// ORCL

		ConceptBound leftORCL = new ConceptBound(15); // create bounds for
		// AMAT
		ConceptBound rightORCL = new ConceptBound(15);
		ConceptBound[][] ORCLBounds = new ConceptBound[2][1];
		ORCLBounds[0][0] = leftORCL;
		ORCLBounds[1][0] = rightORCL;

		ConceptNode ORCL = new ConceptNode("ORCL", 3, ORCLBounds);// create
		// AMAT

		
		//create CSCO
		ConceptBound leftCSCO = new ConceptBound(16); // create bounds for
		// AMAT
		ConceptBound rightCSCO = new ConceptBound(16);
		ConceptBound[][] CSCOBounds = new ConceptBound[2][1];
		CSCOBounds[0][0] = leftCSCO;
		CSCOBounds[1][0] = rightCSCO;

		ConceptNode CSCO = new ConceptNode("CSCO", 3, CSCOBounds);// create

		
		
		ConceptNode[] level3Nodes = new ConceptNode[10];
		level3Nodes[0] = INTC;
		level3Nodes[1] = MSFT;
		level3Nodes[2] = DELL;
		level3Nodes[3] = QQQ;
		level3Nodes[4] = YHOO;
		level3Nodes[5] = RIMM;
		level3Nodes[6] = IPIX;
		level3Nodes[7] = AMAT;
		level3Nodes[8] = ORCL;
		level3Nodes[9] = CSCO;

		ConceptLevel level3 = new ConceptLevel(3, level3Nodes);// create level 3

		ConceptLevel[] levels = new ConceptLevel[3];
		levels[0] = level1;
		levels[1] = level2;
		levels[2] = level3;

		ConceptTree tree = new ConceptTree(levels);

		return tree;
	}

	public static ConceptTree createTree3() {
		ConceptBound leftUSA = new ConceptBound(1); // create bounds for USA
		ConceptBound rightUSA = new ConceptBound(15);
		ConceptBound[][] USABounds = new ConceptBound[2][1];
		USABounds[0][0] = leftUSA;
		USABounds[1][0] = rightUSA;

		ConceptNode USA = new ConceptNode("USA", 1, USABounds);// create USA
		// node

		ConceptNode[] level1Nodes = new ConceptNode[1];
		level1Nodes[0] = USA;
		ConceptLevel level1 = new ConceptLevel(1, level1Nodes);// create level 1

		ConceptBound leftTX = new ConceptBound(2); // create bounds for TX
		ConceptBound rightTX = new ConceptBound(6);
		ConceptBound[][] TXBounds = new ConceptBound[2][1];
		TXBounds[0][0] = leftTX;
		TXBounds[1][0] = rightTX;

		ConceptNode TX = new ConceptNode("TX", 2, TXBounds);// create TX node

		ConceptBound leftOK = new ConceptBound(7); // create bounds for OK
		ConceptBound rightOK = new ConceptBound(10);
		ConceptBound[][] OKBounds = new ConceptBound[2][1];
		OKBounds[0][0] = leftOK;
		OKBounds[1][0] = rightOK;

		ConceptNode OK = new ConceptNode("OK", 2, OKBounds);// create OK node

		ConceptBound leftMA = new ConceptBound(11); // create bounds for OK
		ConceptBound rightMA = new ConceptBound(14);
		ConceptBound[][] MABounds = new ConceptBound[2][1];
		MABounds[0][0] = leftMA;
		MABounds[1][0] = rightMA;

		ConceptNode MA = new ConceptNode("MA", 2, MABounds);// create OK node

		ConceptNode[] level2Nodes = new ConceptNode[3];
		level2Nodes[0] = TX;
		level2Nodes[1] = OK;
		level2Nodes[2] = MA;
		ConceptLevel level2 = new ConceptLevel(2, level2Nodes);// create level 2

		ConceptBound leftGalveston = new ConceptBound(3); // create bounds for
		// Galveston
		ConceptBound rightGalveston = new ConceptBound(3);
		ConceptBound[][] GalvestonBounds = new ConceptBound[2][1];
		GalvestonBounds[0][0] = leftGalveston;
		GalvestonBounds[1][0] = rightGalveston;

		ConceptNode Galveston = new ConceptNode("Galveston", 3, GalvestonBounds);// create
		// Galveston
		// node

		ConceptBound leftAustin = new ConceptBound(4); // create bounds for
		// Austin
		ConceptBound rightAustin = new ConceptBound(4);
		ConceptBound[][] AustinBounds = new ConceptBound[2][1];
		AustinBounds[0][0] = leftAustin;
		AustinBounds[1][0] = rightAustin;

		ConceptNode Austin = new ConceptNode("Austin", 3, AustinBounds);// create
		// Austin
		// node

		ConceptBound leftDallas = new ConceptBound(5); // create bounds for
		// Dallas
		ConceptBound rightDallas = new ConceptBound(5);
		ConceptBound[][] DallasBounds = new ConceptBound[2][1];
		DallasBounds[0][0] = leftDallas;
		DallasBounds[1][0] = rightDallas;

		ConceptNode Dallas = new ConceptNode("Dallas", 3, DallasBounds);// create
		// Dallas
		// node

		ConceptBound leftOKCity = new ConceptBound(8); // create bounds for
		// OKCity
		ConceptBound rightOKCity = new ConceptBound(8);
		ConceptBound[][] OKCityBounds = new ConceptBound[2][1];
		OKCityBounds[0][0] = leftOKCity;
		OKCityBounds[1][0] = rightOKCity;

		ConceptNode OKCity = new ConceptNode("OKCity", 3, OKCityBounds);// create
		// OKCity
		// node

		ConceptBound leftTulsa = new ConceptBound(9); // create bounds for Tulsa
		ConceptBound rightTulsa = new ConceptBound(9);
		ConceptBound[][] TulsaBounds = new ConceptBound[2][1];
		TulsaBounds[0][0] = leftTulsa;
		TulsaBounds[1][0] = rightTulsa;

		ConceptNode Tulsa = new ConceptNode("Tulsa", 3, TulsaBounds);// create
		// Tulsa
		// node

		ConceptBound leftWorcester = new ConceptBound(12); // create bounds for
		// Tulsa
		ConceptBound rightWorcester = new ConceptBound(12);
		ConceptBound[][] WorcesterBounds = new ConceptBound[2][1];
		WorcesterBounds[0][0] = leftWorcester;
		WorcesterBounds[1][0] = rightWorcester;

		ConceptNode Worcester = new ConceptNode("Worcester", 3, WorcesterBounds);// create
		// Tulsa
		// node

		ConceptBound leftBoston = new ConceptBound(13); // create bounds for
		// Tulsa
		ConceptBound rightBoston = new ConceptBound(13);
		ConceptBound[][] BostonBounds = new ConceptBound[2][1];
		BostonBounds[0][0] = leftWorcester;
		BostonBounds[1][0] = rightWorcester;

		ConceptNode Boston = new ConceptNode("Boston", 3, BostonBounds);// create
		// Tulsa
		// node

		ConceptNode[] level3Nodes = new ConceptNode[7];
		level3Nodes[0] = Galveston;
		level3Nodes[1] = Austin;
		level3Nodes[2] = Dallas;
		level3Nodes[3] = OKCity;
		level3Nodes[4] = Tulsa;
		level3Nodes[5] = Worcester;
		level3Nodes[6] = Boston;
		ConceptLevel level3 = new ConceptLevel(3, level3Nodes);// create level 3

		ConceptLevel[] levels = new ConceptLevel[3];
		levels[0] = level1;
		levels[1] = level2;
		levels[2] = level3;

		ConceptTree tree = new ConceptTree(levels);

		return tree;
	}

	// build the concept hierarchy tree
	public static ConceptTree createTree() {
		ConceptBound leftUSA = new ConceptBound(1); // create bounds for USA
		ConceptBound rightUSA = new ConceptBound(11);
		ConceptBound[][] USABounds = new ConceptBound[2][1];
		USABounds[0][0] = leftUSA;
		USABounds[1][0] = rightUSA;

		ConceptNode USA = new ConceptNode("USA", 1, USABounds);// create USA
		// node

		ConceptNode[] level1Nodes = new ConceptNode[1];
		level1Nodes[0] = USA;
		ConceptLevel level1 = new ConceptLevel(1, level1Nodes);// create level 1

		ConceptBound leftTX = new ConceptBound(2); // create bounds for TX
		ConceptBound rightTX = new ConceptBound(6);
		ConceptBound[][] TXBounds = new ConceptBound[2][1];
		TXBounds[0][0] = leftTX;
		TXBounds[1][0] = rightTX;

		ConceptNode TX = new ConceptNode("TX", 2, TXBounds);// create TX node

		ConceptBound leftOK = new ConceptBound(7); // create bounds for OK
		ConceptBound rightOK = new ConceptBound(10);
		ConceptBound[][] OKBounds = new ConceptBound[2][1];
		OKBounds[0][0] = leftOK;
		OKBounds[1][0] = rightOK;

		ConceptNode OK = new ConceptNode("OK", 2, OKBounds);// create OK node

		ConceptNode[] level2Nodes = new ConceptNode[2];
		level2Nodes[0] = TX;
		level2Nodes[1] = OK;
		ConceptLevel level2 = new ConceptLevel(2, level2Nodes);// create level 2

		ConceptBound leftGalveston = new ConceptBound(3); // create bounds for
		// Galveston
		ConceptBound rightGalveston = new ConceptBound(3);
		ConceptBound[][] GalvestonBounds = new ConceptBound[2][1];
		GalvestonBounds[0][0] = leftGalveston;
		GalvestonBounds[1][0] = rightGalveston;

		ConceptNode Galveston = new ConceptNode("Galveston", 3, GalvestonBounds);// create
		// Galveston
		// node

		ConceptBound leftAustin = new ConceptBound(4); // create bounds for
		// Austin
		ConceptBound rightAustin = new ConceptBound(4);
		ConceptBound[][] AustinBounds = new ConceptBound[2][1];
		AustinBounds[0][0] = leftAustin;
		AustinBounds[1][0] = rightAustin;

		ConceptNode Austin = new ConceptNode("Austin", 3, AustinBounds);// create
		// Austin
		// node

		ConceptBound leftDallas = new ConceptBound(5); // create bounds for
		// Dallas
		ConceptBound rightDallas = new ConceptBound(5);
		ConceptBound[][] DallasBounds = new ConceptBound[2][1];
		DallasBounds[0][0] = leftDallas;
		DallasBounds[1][0] = rightDallas;

		ConceptNode Dallas = new ConceptNode("Dallas", 3, DallasBounds);// create
		// Dallas
		// node

		ConceptBound leftOKCity = new ConceptBound(8); // create bounds for
		// OKCity
		ConceptBound rightOKCity = new ConceptBound(8);
		ConceptBound[][] OKCityBounds = new ConceptBound[2][1];
		OKCityBounds[0][0] = leftOKCity;
		OKCityBounds[1][0] = rightOKCity;

		ConceptNode OKCity = new ConceptNode("OKCity", 3, OKCityBounds);// create
		// OKCity
		// node

		ConceptBound leftTulsa = new ConceptBound(9); // create bounds for Tulsa
		ConceptBound rightTulsa = new ConceptBound(9);
		ConceptBound[][] TulsaBounds = new ConceptBound[2][1];
		TulsaBounds[0][0] = leftTulsa;
		TulsaBounds[1][0] = rightTulsa;

		ConceptNode Tulsa = new ConceptNode("Tulsa", 3, TulsaBounds);// create
		// Tulsa
		// node

		ConceptNode[] level3Nodes = new ConceptNode[5];
		level3Nodes[0] = Galveston;
		level3Nodes[1] = Austin;
		level3Nodes[2] = Dallas;
		level3Nodes[3] = OKCity;
		level3Nodes[4] = Tulsa;
		ConceptLevel level3 = new ConceptLevel(3, level3Nodes);// create level 3

		ConceptLevel[] levels = new ConceptLevel[3];
		levels[0] = level1;
		levels[1] = level2;
		levels[2] = level3;

		ConceptTree tree = new ConceptTree(levels);

		return tree;
	}
	
	
	public static ConceptTree createTreePrice()
	{
	    	ConceptBound leftNumber = new ConceptBound(1); //create bounds for number
		ConceptBound rightNumber = new ConceptBound(99);
		ConceptBound[][] numberBounds = new ConceptBound[2][1];
		numberBounds[0][0] = leftNumber;
		numberBounds[1][0] = rightNumber;
		ConceptNode number = new ConceptNode("Number", 1, numberBounds);//create number node
		
		ConceptNode[] level1Nodes = new ConceptNode[1];
		level1Nodes[0] = number;
		ConceptLevel level1 = new ConceptLevel(1, level1Nodes);//create level 1
		
		ConceptBound leftNeg = new ConceptBound(2); 
		ConceptBound rightNeg = new ConceptBound(49);
		ConceptBound[][] NegBounds = new ConceptBound[2][1];
		NegBounds[0][0] = leftNeg;
		NegBounds[1][0] = rightNeg;
		ConceptNode Neg = new ConceptNode("Neg", 2, NegBounds);
		
		ConceptBound leftZero = new ConceptBound(50); 
		ConceptBound rightZero = new ConceptBound(50);
		ConceptBound[][] ZeroBounds = new ConceptBound[2][1];
		ZeroBounds[0][0] = leftZero;
		ZeroBounds[1][0] = rightZero;
		ConceptNode Zero = new ConceptNode("Zero", 2, ZeroBounds);
		
		ConceptBound leftPos = new ConceptBound(51); 
		ConceptBound rightPos = new ConceptBound(98);
		ConceptBound[][] PosBounds = new ConceptBound[2][1];
		PosBounds[0][0] = leftPos;
		PosBounds[1][0] = rightPos;
		ConceptNode Pos = new ConceptNode("Pos", 2, PosBounds);
		
		ConceptNode[] level2Nodes = new ConceptNode[3];
		level2Nodes[0] = Neg;
		level2Nodes[1] = Zero;
		level2Nodes[2] = Pos;
		
		ConceptLevel level2 = new ConceptLevel(2, level2Nodes);//create level 2
		
		
		ConceptBound leftNegge1 = new ConceptBound(3); 
		ConceptBound rightNegge1 = new ConceptBound(17);
		ConceptBound[][] Negge1Bounds = new ConceptBound[2][1];
		Negge1Bounds[0][0] = leftNegge1;
		Negge1Bounds[1][0] = rightNegge1;
		ConceptNode Negge1 = new ConceptNode("Neg>=1", 3, Negge1Bounds);
		
		ConceptBound leftNegl1 = new ConceptBound(18); 
		ConceptBound rightNegl1 = new ConceptBound(48);
		ConceptBound[][] Negl1Bounds = new ConceptBound[2][1];
		Negl1Bounds[0][0] = leftNegl1;
		Negl1Bounds[1][0] = rightNegl1;
		ConceptNode Negl1 = new ConceptNode("Neg<1", 3, Negl1Bounds);
		
		ConceptBound leftPosge1 = new ConceptBound(52); 
		ConceptBound rightPosge1 = new ConceptBound(66);
		ConceptBound[][] Posge1Bounds = new ConceptBound[2][1];
		Posge1Bounds[0][0] = leftPosge1;
		Posge1Bounds[1][0] = rightPosge1;
		ConceptNode Posge1 = new ConceptNode("Pos>=1", 3, Posge1Bounds);
		
		ConceptBound leftPosl1 = new ConceptBound(67); 
		ConceptBound rightPosl1 = new ConceptBound(97);
		ConceptBound[][] Posl1Bounds = new ConceptBound[2][1];
		Posl1Bounds[0][0] = leftPosl1;
		Posl1Bounds[1][0] = rightPosl1;
		ConceptNode Posl1 = new ConceptNode("Pos<1", 3, NegBounds);
		
		ConceptNode[] level3Nodes = new ConceptNode[4];
		level3Nodes[0] = Negge1;
		level3Nodes[1] = Negl1;
		level3Nodes[2] = Posge1;
		level3Nodes[3] = Posl1;
		ConceptLevel level3 = new ConceptLevel(3, level3Nodes);//create level 3
		
		
		ConceptBound leftNeg1_2 = new ConceptBound(4); 
		ConceptBound rightNeg1_2 = new ConceptBound(7);
		ConceptBound[][] Neg1_2Bounds = new ConceptBound[2][1];
		Neg1_2Bounds[0][0] = leftNeg1_2;
		Neg1_2Bounds[1][0] = rightNeg1_2;
		ConceptNode Neg1_2 = new ConceptNode("Neg1_2", 4, Neg1_2Bounds);
		
		ConceptBound leftNeg2_3 = new ConceptBound(8); 
		ConceptBound rightNeg2_3 = new ConceptBound(11);
		ConceptBound[][] Neg2_3Bounds = new ConceptBound[2][1];
		Neg2_3Bounds[0][0] = leftNeg2_3;
		Neg2_3Bounds[1][0] = rightNeg2_3;
		ConceptNode Neg2_3 = new ConceptNode("Neg2_3", 4, Neg2_3Bounds);
		
		ConceptBound leftNeg3_4 = new ConceptBound(12); 
		ConceptBound rightNeg3_4 = new ConceptBound(15);
		ConceptBound[][] Neg3_4Bounds = new ConceptBound[2][1];
		Neg3_4Bounds[0][0] = leftNeg3_4;
		Neg3_4Bounds[1][0] = rightNeg3_4;
		ConceptNode Neg3_4 = new ConceptNode("Neg3_4", 4, Neg3_4Bounds);
		
		ConceptBound leftNegge4 = new ConceptBound(16); 
		ConceptBound rightNegge4 = new ConceptBound(16);
		ConceptBound[][] Negge4Bounds = new ConceptBound[2][1];
		Negge4Bounds[0][0] = leftNegge4;
		Negge4Bounds[1][0] = rightNegge4;
		ConceptNode Negge4 = new ConceptNode("Negge4", 4, Negge4Bounds);
		
		ConceptBound leftNeg0_p5 = new ConceptBound(19); 
		ConceptBound rightNeg0_p5 = new ConceptBound(40);
		ConceptBound[][] Neg0_p5Bounds = new ConceptBound[2][1];
		Neg0_p5Bounds[0][0] = leftNeg0_p5;
		Neg0_p5Bounds[1][0] = rightNeg0_p5;
		ConceptNode Neg0_p5 = new ConceptNode("Neg0_p5", 4, Neg0_p5Bounds);
		
		ConceptBound leftNegp5_1 = new ConceptBound(41); 
		ConceptBound rightNegp5_1 = new ConceptBound(47);
		ConceptBound[][] Negp5_1Bounds = new ConceptBound[2][1];
		Negp5_1Bounds[0][0] = leftNegp5_1;
		Negp5_1Bounds[1][0] = rightNegp5_1;
		ConceptNode Negp5_1 = new ConceptNode("Negp5_1", 4, Negp5_1Bounds);
		
		ConceptBound leftPos1_2 = new ConceptBound(53); 
		ConceptBound rightPos1_2 = new ConceptBound(56);
		ConceptBound[][] Pos1_2Bounds = new ConceptBound[2][1];
		Pos1_2Bounds[0][0] = leftPos1_2;
		Pos1_2Bounds[1][0] = rightPos1_2;
		ConceptNode Pos1_2 = new ConceptNode("Pos1_2", 4, Pos1_2Bounds);
		
		ConceptBound leftPos2_3 = new ConceptBound(57); 
		ConceptBound rightPos2_3 = new ConceptBound(60);
		ConceptBound[][] Pos2_3Bounds = new ConceptBound[2][1];
		Pos2_3Bounds[0][0] = leftPos2_3;
		Pos2_3Bounds[1][0] = rightPos2_3;
		ConceptNode Pos2_3 = new ConceptNode("Pos2_3", 4, Pos2_3Bounds);
		
		ConceptBound leftPos3_4 = new ConceptBound(61); 
		ConceptBound rightPos3_4 = new ConceptBound(64);
		ConceptBound[][] Pos3_4Bounds = new ConceptBound[2][1];
		Pos3_4Bounds[0][0] = leftPos3_4;
		Pos3_4Bounds[1][0] = rightPos3_4;
		ConceptNode Pos3_4 = new ConceptNode("Pos3_4", 4, Pos3_4Bounds);
		
		ConceptBound leftPosge4 = new ConceptBound(65); 
		ConceptBound rightPosge4 = new ConceptBound(65);
		ConceptBound[][] Posge4Bounds = new ConceptBound[2][1];
		Posge4Bounds[0][0] = leftPosge4;
		Posge4Bounds[1][0] = rightPosge4;
		ConceptNode Posge4 = new ConceptNode("Posge4", 4, Posge4Bounds);
		
		ConceptBound leftPos0_p5 = new ConceptBound(68); 
		ConceptBound rightPos0_p5 = new ConceptBound(89);
		ConceptBound[][] Pos0_p5Bounds = new ConceptBound[2][1];
		Pos0_p5Bounds[0][0] = leftPos0_p5;
		Pos0_p5Bounds[1][0] = rightPos0_p5;
		ConceptNode Pos0_p5 = new ConceptNode("Pos0_p5", 4, Pos0_p5Bounds);
		
		ConceptBound leftPosp5_1 = new ConceptBound(90); 
		ConceptBound rightPosp5_1 = new ConceptBound(96);
		ConceptBound[][] Posp5_1Bounds = new ConceptBound[2][1];
		Posp5_1Bounds[0][0] = leftPosp5_1;
		Posp5_1Bounds[1][0] = rightPosp5_1;
		ConceptNode Posp5_1 = new ConceptNode("Posp5_1", 4, Posp5_1Bounds);
		
		ConceptNode[] level4Nodes = new ConceptNode[12];
		level4Nodes[0] = Neg1_2;
		level4Nodes[1] = Neg2_3;
		level4Nodes[2] = Neg3_4;
		level4Nodes[3] = Negge4;
		level4Nodes[4] = Neg0_p5;
		level4Nodes[5] = Negp5_1;
		level4Nodes[6] = Pos1_2;
		level4Nodes[7] = Pos2_3;
		level4Nodes[8] = Pos3_4;
		level4Nodes[9] = Posge4;
		level4Nodes[10] = Pos0_p5;
		level4Nodes[11] = Posp5_1;
		ConceptLevel level4 = new ConceptLevel(4, level4Nodes);//create level 4
		
		
		ConceptBound leftNeg1_1p5 = new ConceptBound(5); 
		ConceptBound rightNeg1_1p5 = new ConceptBound(5);
		ConceptBound[][] Neg1_1p5Bounds = new ConceptBound[2][1];
		Neg1_1p5Bounds[0][0] = leftNeg1_1p5;
		Neg1_1p5Bounds[1][0] = rightNeg1_1p5;
		ConceptNode Neg1_1p5 = new ConceptNode("Neg1_1p5", 5, Neg1_1p5Bounds);
		
		ConceptBound leftNeg1p5_2 = new ConceptBound(6); 
		ConceptBound rightNeg1p5_2 = new ConceptBound(6);
		ConceptBound[][] Neg1p5_2Bounds = new ConceptBound[2][1];
		Neg1p5_2Bounds[0][0] = leftNeg1p5_2;
		Neg1p5_2Bounds[1][0] = rightNeg1p5_2;
		ConceptNode Neg1p5_2 = new ConceptNode("Neg1p5_2", 5, Neg1p5_2Bounds);
		
		ConceptBound leftNeg2_2p5 = new ConceptBound(9); 
		ConceptBound rightNeg2_2p5 = new ConceptBound(9);
		ConceptBound[][] Neg2_2p5Bounds = new ConceptBound[3][2];
		Neg2_2p5Bounds[0][0] = leftNeg2_2p5;
		Neg2_2p5Bounds[2][0] = rightNeg2_2p5;
		ConceptNode Neg2_2p5 = new ConceptNode("Neg2_2p5", 5, Neg2_2p5Bounds);
		
		ConceptBound leftNeg2p5_3 = new ConceptBound(10); 
		ConceptBound rightNeg2p5_3 = new ConceptBound(10);
		ConceptBound[][] Neg2p5_3Bounds = new ConceptBound[3][2];
		Neg2p5_3Bounds[0][0] = leftNeg2p5_3;
		Neg2p5_3Bounds[2][0] = rightNeg2p5_3;
		ConceptNode Neg2p5_3 = new ConceptNode("Neg2p5_3", 5, Neg2p5_3Bounds);
		
		ConceptBound leftNeg3_3p5 = new ConceptBound(13); 
		ConceptBound rightNeg3_3p5 = new ConceptBound(13);
		ConceptBound[][] Neg3_3p5Bounds = new ConceptBound[4][3];
		Neg3_3p5Bounds[0][0] = leftNeg3_3p5;
		Neg3_3p5Bounds[3][0] = rightNeg3_3p5;
		ConceptNode Neg3_3p5 = new ConceptNode("Neg3_3p5", 5, Neg3_3p5Bounds);
		
		ConceptBound leftNeg3p5_4 = new ConceptBound(14); 
		ConceptBound rightNeg3p5_4 = new ConceptBound(14);
		ConceptBound[][] Neg3p5_4Bounds = new ConceptBound[4][3];
		Neg3p5_4Bounds[0][0] = leftNeg3p5_4;
		Neg3p5_4Bounds[3][0] = rightNeg3p5_4;
		ConceptNode Neg3p5_4 = new ConceptNode("Neg3p5_4", 5, Neg3p5_4Bounds);
		
		ConceptBound leftNeg0_p1 = new ConceptBound(20); 
		ConceptBound rightNeg0_p1 = new ConceptBound(23);
		ConceptBound[][] Neg0_p1Bounds = new ConceptBound[4][3];
		Neg0_p1Bounds[0][0] = leftNeg0_p1;
		Neg0_p1Bounds[3][0] = rightNeg0_p1;
		ConceptNode Neg0_p1 = new ConceptNode("Neg0_p1", 5, Neg0_p1Bounds);
		
		ConceptBound leftNegp1_p2 = new ConceptBound(24); 
		ConceptBound rightNegp1_p2 = new ConceptBound(27);
		ConceptBound[][] Negp1_p2Bounds = new ConceptBound[4][3];
		Negp1_p2Bounds[0][0] = leftNegp1_p2;
		Negp1_p2Bounds[3][0] = rightNegp1_p2;
		ConceptNode Negp1_p2 = new ConceptNode("Negp1_p2", 5, Negp1_p2Bounds);
		
		ConceptBound leftNegp2_p3 = new ConceptBound(28); 
		ConceptBound rightNegp2_p3 = new ConceptBound(31);
		ConceptBound[][] Negp2_p3Bounds = new ConceptBound[4][3];
		Negp2_p3Bounds[0][0] = leftNegp2_p3;
		Negp2_p3Bounds[3][0] = rightNegp2_p3;
		ConceptNode Negp2_p3 = new ConceptNode("Negp2_p3", 5, Negp2_p3Bounds);
		
		ConceptBound leftNegp3_p4 = new ConceptBound(32); 
		ConceptBound rightNegp3_p4 = new ConceptBound(35);
		ConceptBound[][] Negp3_p4Bounds = new ConceptBound[4][3];
		Negp3_p4Bounds[0][0] = leftNegp3_p4;
		Negp3_p4Bounds[3][0] = rightNegp3_p4;
		ConceptNode Negp3_p4 = new ConceptNode("Negp3_p4", 5, Negp3_p4Bounds);
		
		ConceptBound leftNegp4_p5 = new ConceptBound(36); 
		ConceptBound rightNegp4_p5 = new ConceptBound(39);
		ConceptBound[][] Negp4_p5Bounds = new ConceptBound[4][3];
		Negp4_p5Bounds[0][0] = leftNegp4_p5;
		Negp4_p5Bounds[3][0] = rightNegp4_p5;
		ConceptNode Negp4_p5 = new ConceptNode("Negp4_p5", 5, Negp4_p5Bounds);
		
		ConceptBound leftNegp5_p6 = new ConceptBound(42); 
		ConceptBound rightNegp5_p6 = new ConceptBound(42);
		ConceptBound[][] Negp5_p6Bounds = new ConceptBound[4][3];
		Negp5_p6Bounds[0][0] = leftNegp5_p6;
		Negp5_p6Bounds[3][0] = rightNegp5_p6;
		ConceptNode Negp5_p6 = new ConceptNode("Negp5_p6", 5, Negp5_p6Bounds);
		
		ConceptBound leftNegp6_p7 = new ConceptBound(43); 
		ConceptBound rightNegp6_p7 = new ConceptBound(43);
		ConceptBound[][] Negp6_p7Bounds = new ConceptBound[4][3];
		Negp6_p7Bounds[0][0] = leftNegp6_p7;
		Negp6_p7Bounds[3][0] = rightNegp6_p7;
		ConceptNode Negp6_p7 = new ConceptNode("Negp6_p7", 5, Negp6_p7Bounds);
		
		ConceptBound leftNegp7_p8 = new ConceptBound(44); 
		ConceptBound rightNegp7_p8 = new ConceptBound(44);
		ConceptBound[][] Negp7_p8Bounds = new ConceptBound[4][3];
		Negp7_p8Bounds[0][0] = leftNegp7_p8;
		Negp7_p8Bounds[3][0] = rightNegp7_p8;
		ConceptNode Negp7_p8 = new ConceptNode("Negp7_p8", 5, Negp7_p8Bounds);
		
		ConceptBound leftNegp8_p9 = new ConceptBound(45); 
		ConceptBound rightNegp8_p9 = new ConceptBound(45);
		ConceptBound[][] Negp8_p9Bounds = new ConceptBound[4][3];
		Negp8_p9Bounds[0][0] = leftNegp8_p9;
		Negp8_p9Bounds[3][0] = rightNegp8_p9;
		ConceptNode Negp8_p9 = new ConceptNode("Negp8_p9", 5, Negp8_p9Bounds);
		
		ConceptBound leftNegp9_1 = new ConceptBound(46); 
		ConceptBound rightNegp9_1 = new ConceptBound(46);
		ConceptBound[][] Negp9_1Bounds = new ConceptBound[4][3];
		Negp9_1Bounds[0][0] = leftNegp9_1;
		Negp9_1Bounds[3][0] = rightNegp9_1;
		ConceptNode Negp9_1 = new ConceptNode("Negp9_1", 5, Negp9_1Bounds);
		
		
		ConceptBound leftPos1_1p5 = new ConceptBound(54); 
		ConceptBound rightPos1_1p5 = new ConceptBound(54);
		ConceptBound[][] Pos1_1p5Bounds = new ConceptBound[2][1];
		Pos1_1p5Bounds[0][0] = leftPos1_1p5;
		Pos1_1p5Bounds[1][0] = rightPos1_1p5;
		ConceptNode Pos1_1p5 = new ConceptNode("Pos1_1p5", 5, Pos1_1p5Bounds);
		
		ConceptBound leftPos1p5_2 = new ConceptBound(55); 
		ConceptBound rightPos1p5_2 = new ConceptBound(55);
		ConceptBound[][] Pos1p5_2Bounds = new ConceptBound[2][1];
		Pos1p5_2Bounds[0][0] = leftPos1p5_2;
		Pos1p5_2Bounds[1][0] = rightPos1p5_2;
		ConceptNode Pos1p5_2 = new ConceptNode("Pos1p5_2", 5, Pos1p5_2Bounds);
		
		ConceptBound leftPos2_2p5 = new ConceptBound(58); 
		ConceptBound rightPos2_2p5 = new ConceptBound(58);
		ConceptBound[][] Pos2_2p5Bounds = new ConceptBound[3][2];
		Pos2_2p5Bounds[0][0] = leftPos2_2p5;
		Pos2_2p5Bounds[2][0] = rightPos2_2p5;
		ConceptNode Pos2_2p5 = new ConceptNode("Pos2_2p5", 5, Pos2_2p5Bounds);
		
		ConceptBound leftPos2p5_3 = new ConceptBound(59); 
		ConceptBound rightPos2p5_3 = new ConceptBound(59);
		ConceptBound[][] Pos2p5_3Bounds = new ConceptBound[3][2];
		Pos2p5_3Bounds[0][0] = leftPos2p5_3;
		Pos2p5_3Bounds[2][0] = rightPos2p5_3;
		ConceptNode Pos2p5_3 = new ConceptNode("Pos2p5_3", 5, Pos2p5_3Bounds);
		
		ConceptBound leftPos3_3p5 = new ConceptBound(62); 
		ConceptBound rightPos3_3p5 = new ConceptBound(62);
		ConceptBound[][] Pos3_3p5Bounds = new ConceptBound[4][3];
		Pos3_3p5Bounds[0][0] = leftPos3_3p5;
		Pos3_3p5Bounds[3][0] = rightPos3_3p5;
		ConceptNode Pos3_3p5 = new ConceptNode("Pos3_3p5", 5, Pos3_3p5Bounds);
		
		ConceptBound leftPos3p5_4 = new ConceptBound(63); 
		ConceptBound rightPos3p5_4 = new ConceptBound(63);
		ConceptBound[][] Pos3p5_4Bounds = new ConceptBound[4][3];
		Pos3p5_4Bounds[0][0] = leftPos3p5_4;
		Pos3p5_4Bounds[3][0] = rightPos3p5_4;
		ConceptNode Pos3p5_4 = new ConceptNode("Pos3p5_4", 5, Pos3p5_4Bounds);
		
		ConceptBound leftPos0_p1 = new ConceptBound(69); 
		ConceptBound rightPos0_p1 = new ConceptBound(72);
		ConceptBound[][] Pos0_p1Bounds = new ConceptBound[4][3];
		Pos0_p1Bounds[0][0] = leftPos0_p1;
		Pos0_p1Bounds[3][0] = rightPos0_p1;
		ConceptNode Pos0_p1 = new ConceptNode("Pos0_p1", 5, Pos0_p1Bounds);
		
		ConceptBound leftPosp1_p2 = new ConceptBound(73); 
		ConceptBound rightPosp1_p2 = new ConceptBound(76);
		ConceptBound[][] Posp1_p2Bounds = new ConceptBound[4][3];
		Posp1_p2Bounds[0][0] = leftPosp1_p2;
		Posp1_p2Bounds[3][0] = rightPosp1_p2;
		ConceptNode Posp1_p2 = new ConceptNode("Posp1_p2", 5, Posp1_p2Bounds);
		
		ConceptBound leftPosp2_p3 = new ConceptBound(77); 
		ConceptBound rightPosp2_p3 = new ConceptBound(80);
		ConceptBound[][] Posp2_p3Bounds = new ConceptBound[4][3];
		Posp2_p3Bounds[0][0] = leftPosp2_p3;
		Posp2_p3Bounds[3][0] = rightPosp2_p3;
		ConceptNode Posp2_p3 = new ConceptNode("Posp2_p3", 5, Posp2_p3Bounds);
		
		ConceptBound leftPosp3_p4 = new ConceptBound(81); 
		ConceptBound rightPosp3_p4 = new ConceptBound(84);
		ConceptBound[][] Posp3_p4Bounds = new ConceptBound[4][3];
		Posp3_p4Bounds[0][0] = leftPosp3_p4;
		Posp3_p4Bounds[3][0] = rightPosp3_p4;
		ConceptNode Posp3_p4 = new ConceptNode("Posp3_p4", 5, Posp3_p4Bounds);
		
		ConceptBound leftPosp4_p5 = new ConceptBound(85); 
		ConceptBound rightPosp4_p5 = new ConceptBound(88);
		ConceptBound[][] Posp4_p5Bounds = new ConceptBound[4][3];
		Posp4_p5Bounds[0][0] = leftPosp4_p5;
		Posp4_p5Bounds[3][0] = rightPosp4_p5;
		ConceptNode Posp4_p5 = new ConceptNode("Posp4_p5", 5, Posp4_p5Bounds);
		
		ConceptBound leftPosp5_p6 = new ConceptBound(91); 
		ConceptBound rightPosp5_p6 = new ConceptBound(91);
		ConceptBound[][] Posp5_p6Bounds = new ConceptBound[4][3];
		Posp5_p6Bounds[0][0] = leftPosp5_p6;
		Posp5_p6Bounds[3][0] = rightPosp5_p6;
		ConceptNode Posp5_p6 = new ConceptNode("Posp5_p6", 5, Posp5_p6Bounds);
		
		ConceptBound leftPosp6_p7 = new ConceptBound(92); 
		ConceptBound rightPosp6_p7 = new ConceptBound(92);
		ConceptBound[][] Posp6_p7Bounds = new ConceptBound[4][3];
		Posp6_p7Bounds[0][0] = leftPosp6_p7;
		Posp6_p7Bounds[3][0] = rightPosp6_p7;
		ConceptNode Posp6_p7 = new ConceptNode("Posp6_p7", 5, Posp6_p7Bounds);
		
		ConceptBound leftPosp7_p8 = new ConceptBound(93); 
		ConceptBound rightPosp7_p8 = new ConceptBound(93);
		ConceptBound[][] Posp7_p8Bounds = new ConceptBound[4][3];
		Posp7_p8Bounds[0][0] = leftPosp7_p8;
		Posp7_p8Bounds[3][0] = rightPosp7_p8;
		ConceptNode Posp7_p8 = new ConceptNode("Posp7_p8", 5, Posp7_p8Bounds);
		
		ConceptBound leftPosp8_p9 = new ConceptBound(94); 
		ConceptBound rightPosp8_p9 = new ConceptBound(94);
		ConceptBound[][] Posp8_p9Bounds = new ConceptBound[4][3];
		Posp8_p9Bounds[0][0] = leftPosp8_p9;
		Posp8_p9Bounds[3][0] = rightPosp8_p9;
		ConceptNode Posp8_p9 = new ConceptNode("Posp8_p9", 5, Posp8_p9Bounds);
		
		ConceptBound leftPosp9_1 = new ConceptBound(95); 
		ConceptBound rightPosp9_1 = new ConceptBound(95);
		ConceptBound[][] Posp9_1Bounds = new ConceptBound[4][3];
		Posp9_1Bounds[0][0] = leftPosp9_1;
		Posp9_1Bounds[3][0] = rightPosp9_1;
		ConceptNode Posp9_1 = new ConceptNode("Posp9_1", 5, Posp9_1Bounds);
		
		
		ConceptNode[] level5Nodes = new ConceptNode[32];
		level5Nodes[0] = Neg1_1p5;
		level5Nodes[1] = Neg1p5_2;
		level5Nodes[2] = Neg2_2p5;
		level5Nodes[3] = Neg2p5_3;
		level5Nodes[4] = Neg3_3p5;
		level5Nodes[5] = Neg3p5_4;
		level5Nodes[6] = Neg0_p1;
		level5Nodes[7] = Negp1_p2;
		level5Nodes[8] = Negp2_p3;
		level5Nodes[9] = Negp3_p4;
		level5Nodes[10] = Negp4_p5;
		level5Nodes[11] = Negp5_p6;
		level5Nodes[12] = Negp6_p7;
		level5Nodes[13] = Negp7_p8;
		level5Nodes[14] = Negp8_p9;
		level5Nodes[15] = Negp9_1;
		level5Nodes[16] = Pos1_1p5;
		level5Nodes[17] = Pos1p5_2;
		level5Nodes[18] = Pos2_2p5;
		level5Nodes[19] = Pos2p5_3;
		level5Nodes[20] = Pos3_3p5;
		level5Nodes[21] = Pos3p5_4;
		level5Nodes[22] = Pos0_p1;
		level5Nodes[23] = Posp1_p2;
		level5Nodes[24] = Posp2_p3;
		level5Nodes[25] = Posp3_p4;
		level5Nodes[26] = Posp4_p5;
		level5Nodes[27] = Posp5_p6;
		level5Nodes[28] = Posp6_p7;
		level5Nodes[29] = Posp7_p8;
		level5Nodes[30] = Posp8_p9;
		level5Nodes[31] = Posp9_1;
		ConceptLevel level5 = new ConceptLevel(5, level5Nodes);//create level 5
		
		ConceptBound leftNeg0_p05 = new ConceptBound(21); 
		ConceptBound rightNeg0_p05 = new ConceptBound(21);
		ConceptBound[][] Neg0_p05Bounds = new ConceptBound[2][1];
		Neg0_p05Bounds[0][0] = leftNeg0_p05;
		Neg0_p05Bounds[1][0] = rightNeg0_p05;
		ConceptNode Neg0_p05 = new ConceptNode("Neg0_p05", 6, Neg0_p05Bounds);
		
		ConceptBound leftNegp05_p1 = new ConceptBound(22); 
		ConceptBound rightNegp05_p1 = new ConceptBound(22);
		ConceptBound[][] Negp05_p1Bounds = new ConceptBound[2][1];
		Negp05_p1Bounds[0][0] = leftNegp05_p1;
		Negp05_p1Bounds[1][0] = rightNegp05_p1;
		ConceptNode Negp05_p1 = new ConceptNode("Negp05_p1", 6, Negp05_p1Bounds);
		
		ConceptBound leftNegp1_p15 = new ConceptBound(25); 
		ConceptBound rightNegp1_p15 = new ConceptBound(25);
		ConceptBound[][] Negp1_p15Bounds = new ConceptBound[2][1];
		Negp1_p15Bounds[0][0] = leftNegp1_p15;
		Negp1_p15Bounds[1][0] = rightNegp1_p15;
		ConceptNode Negp1_p15 = new ConceptNode("Negp1_p15", 6, Negp1_p15Bounds);
		
		ConceptBound leftNegp15_p2 = new ConceptBound(26); 
		ConceptBound rightNegp15_p2 = new ConceptBound(26);
		ConceptBound[][] Negp15_p2Bounds = new ConceptBound[2][1];
		Negp15_p2Bounds[0][0] = leftNegp15_p2;
		Negp15_p2Bounds[1][0] = rightNegp15_p2;
		ConceptNode Negp15_p2 = new ConceptNode("Negp15_p2", 6, Negp15_p2Bounds);
		
		ConceptBound leftNegp2_p25 = new ConceptBound(29); 
		ConceptBound rightNegp2_p25 = new ConceptBound(29);
		ConceptBound[][] Negp2_p25Bounds = new ConceptBound[2][1];
		Negp2_p25Bounds[0][0] = leftNegp2_p25;
		Negp2_p25Bounds[1][0] = rightNegp2_p25;
		ConceptNode Negp2_p25 = new ConceptNode("Negp2_p25", 6, Negp2_p25Bounds);
		
		ConceptBound leftNegp25_p3 = new ConceptBound(30); 
		ConceptBound rightNegp25_p3 = new ConceptBound(30);
		ConceptBound[][] Negp25_p3Bounds = new ConceptBound[2][1];
		Negp25_p3Bounds[0][0] = leftNegp25_p3;
		Negp25_p3Bounds[1][0] = rightNegp25_p3;
		ConceptNode Negp25_p3 = new ConceptNode("Negp25_p3", 6, Negp25_p3Bounds);
		
		ConceptBound leftNegp3_p35 = new ConceptBound(33); 
		ConceptBound rightNegp3_p35 = new ConceptBound(33);
		ConceptBound[][] Negp3_p35Bounds = new ConceptBound[2][1];
		Negp3_p35Bounds[0][0] = leftNegp3_p35;
		Negp3_p35Bounds[1][0] = rightNegp3_p35;
		ConceptNode Negp3_p35 = new ConceptNode("Negp3_p35", 6, Negp3_p35Bounds);
		
		ConceptBound leftNegp35_p4 = new ConceptBound(34); 
		ConceptBound rightNegp35_p4 = new ConceptBound(34);
		ConceptBound[][] Negp35_p4Bounds = new ConceptBound[2][1];
		Negp35_p4Bounds[0][0] = leftNegp35_p4;
		Negp35_p4Bounds[1][0] = rightNegp35_p4;
		ConceptNode Negp35_p4 = new ConceptNode("Negp35_p4", 6, Negp35_p4Bounds);
		
		ConceptBound leftNegp4_p45 = new ConceptBound(37); 
		ConceptBound rightNegp4_p45 = new ConceptBound(37);
		ConceptBound[][] Negp4_p45Bounds = new ConceptBound[2][1];
		Negp4_p45Bounds[0][0] = leftNegp4_p45;
		Negp4_p45Bounds[1][0] = rightNegp4_p45;
		ConceptNode Negp4_p45 = new ConceptNode("Negp4_p45", 6, Negp4_p45Bounds);
		
		ConceptBound leftNegp45_p5 = new ConceptBound(38); 
		ConceptBound rightNegp45_p5 = new ConceptBound(38);
		ConceptBound[][] Negp45_p5Bounds = new ConceptBound[2][1];
		Negp45_p5Bounds[0][0] = leftNegp45_p5;
		Negp45_p5Bounds[1][0] = rightNegp45_p5;
		ConceptNode Negp45_p5 = new ConceptNode("Negp45_p5", 6, Negp45_p5Bounds);
		
		ConceptBound leftPos0_p05 = new ConceptBound(70); 
		ConceptBound rightPos0_p05 = new ConceptBound(70);
		ConceptBound[][] Pos0_p05Bounds = new ConceptBound[2][1];
		Pos0_p05Bounds[0][0] = leftPos0_p05;
		Pos0_p05Bounds[1][0] = rightPos0_p05;
		ConceptNode Pos0_p05 = new ConceptNode("Pos0_p05", 6, Pos0_p05Bounds);
		
		ConceptBound leftPosp05_p1 = new ConceptBound(71); 
		ConceptBound rightPosp05_p1 = new ConceptBound(71);
		ConceptBound[][] Posp05_p1Bounds = new ConceptBound[2][1];
		Posp05_p1Bounds[0][0] = leftPosp05_p1;
		Posp05_p1Bounds[1][0] = rightPosp05_p1;
		ConceptNode Posp05_p1 = new ConceptNode("Posp05_p1", 6, Posp05_p1Bounds);
		
		ConceptBound leftPosp1_p15 = new ConceptBound(74); 
		ConceptBound rightPosp1_p15 = new ConceptBound(74);
		ConceptBound[][] Posp1_p15Bounds = new ConceptBound[2][1];
		Posp1_p15Bounds[0][0] = leftPosp1_p15;
		Posp1_p15Bounds[1][0] = rightPosp1_p15;
		ConceptNode Posp1_p15 = new ConceptNode("Posp1_p15", 6, Posp1_p15Bounds);
		
		ConceptBound leftPosp15_p2 = new ConceptBound(75); 
		ConceptBound rightPosp15_p2 = new ConceptBound(75);
		ConceptBound[][] Posp15_p2Bounds = new ConceptBound[2][1];
		Posp15_p2Bounds[0][0] = leftPosp15_p2;
		Posp15_p2Bounds[1][0] = rightPosp15_p2;
		ConceptNode Posp15_p2 = new ConceptNode("Posp15_p2", 6, Posp15_p2Bounds);
		
		ConceptBound leftPosp2_p25 = new ConceptBound(78); 
		ConceptBound rightPosp2_p25 = new ConceptBound(78);
		ConceptBound[][] Posp2_p25Bounds = new ConceptBound[2][1];
		Posp2_p25Bounds[0][0] = leftPosp2_p25;
		Posp2_p25Bounds[1][0] = rightPosp2_p25;
		ConceptNode Posp2_p25 = new ConceptNode("Posp2_p25", 6, Posp2_p25Bounds);
		
		ConceptBound leftPosp25_p3 = new ConceptBound(79); 
		ConceptBound rightPosp25_p3 = new ConceptBound(79);
		ConceptBound[][] Posp25_p3Bounds = new ConceptBound[2][1];
		Posp25_p3Bounds[0][0] = leftPosp25_p3;
		Posp25_p3Bounds[1][0] = rightPosp25_p3;
		ConceptNode Posp25_p3 = new ConceptNode("Posp25_p3", 6, Posp25_p3Bounds);
		
		ConceptBound leftPosp3_p35 = new ConceptBound(82); 
		ConceptBound rightPosp3_p35 = new ConceptBound(82);
		ConceptBound[][] Posp3_p35Bounds = new ConceptBound[2][1];
		Posp3_p35Bounds[0][0] = leftPosp3_p35;
		Posp3_p35Bounds[1][0] = rightPosp3_p35;
		ConceptNode Posp3_p35 = new ConceptNode("Posp3_p35", 6, Posp3_p35Bounds);
		
		ConceptBound leftPosp35_p4 = new ConceptBound(83); 
		ConceptBound rightPosp35_p4 = new ConceptBound(83);
		ConceptBound[][] Posp35_p4Bounds = new ConceptBound[2][1];
		Posp35_p4Bounds[0][0] = leftPosp35_p4;
		Posp35_p4Bounds[1][0] = rightPosp35_p4;
		ConceptNode Posp35_p4 = new ConceptNode("Posp35_p4", 6, Posp35_p4Bounds);
		
		ConceptBound leftPosp4_p45 = new ConceptBound(86); 
		ConceptBound rightPosp4_p45 = new ConceptBound(86);
		ConceptBound[][] Posp4_p45Bounds = new ConceptBound[2][1];
		Posp4_p45Bounds[0][0] = leftPosp4_p45;
		Posp4_p45Bounds[1][0] = rightPosp4_p45;
		ConceptNode Posp4_p45 = new ConceptNode("Posp4_p45", 6, Posp4_p45Bounds);
		
		ConceptBound leftPosp45_p5 = new ConceptBound(87); 
		ConceptBound rightPosp45_p5 = new ConceptBound(87);
		ConceptBound[][] Posp45_p5Bounds = new ConceptBound[2][1];
		Posp45_p5Bounds[0][0] = leftPosp45_p5;
		Posp45_p5Bounds[1][0] = rightPosp45_p5;
		ConceptNode Posp45_p5 = new ConceptNode("Posp45_p5", 6, Posp45_p5Bounds);
		
		ConceptNode[] level6Nodes = new ConceptNode[20];
		level6Nodes[0] = Neg0_p05;
		level6Nodes[1] = Negp05_p1;
		level6Nodes[2] = Negp1_p15;
		level6Nodes[3] = Negp15_p2;
		level6Nodes[4] = Negp2_p25;
		level6Nodes[5] = Negp25_p3;
		level6Nodes[6] = Negp3_p35;
		level6Nodes[7] = Negp35_p4;
		level6Nodes[8] = Negp4_p45;
		level6Nodes[9] = Negp45_p5;
		level6Nodes[10] = Pos0_p05;
		level6Nodes[11] = Posp05_p1;
		level6Nodes[12] = Posp1_p15;
		level6Nodes[13] = Posp15_p2;
		level6Nodes[14] = Posp2_p25;
		level6Nodes[15] = Posp25_p3;
		level6Nodes[16] = Posp3_p35;
		level6Nodes[17] = Posp35_p4;
		level6Nodes[18] = Posp4_p45;
		level6Nodes[19] = Posp45_p5;
		ConceptLevel level6 = new ConceptLevel(6, level6Nodes);//create level 6
		
		ConceptLevel[] levels = new ConceptLevel[6];
		levels[0] = level1;
		levels[1] = level2;
		levels[2] = level3;
		levels[3] = level4;
		levels[4] = level5;
		levels[5] = level6;
		
		ConceptTree tree = new ConceptTree(levels);
		
		
		return tree;
		
	}
	


}
