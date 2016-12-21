/**
 * 
 */
package com.hp.hpl.CHAOS.ECube;

/**
 * @author Kara Greenfield
 * 
 */
public class Driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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

		tree.printTree();

	}

}
