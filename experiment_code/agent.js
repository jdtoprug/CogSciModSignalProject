/*this file contains the functions for the agent behaviour used 
in the Bachelor's Project Experiment conducted by Maxime Bos, m.r.m.bos@student.rug.nl*/

function Agent(expID, signal, round){
	this.expID = expID;
	this.round = round;
	this.signal = signal;

	var choice = 0;
	
	this.choice =
		function(expID, signal, round, userChoice, signalList, choiceList, agentWins, agentSignals, totalSignals, allUserChoices){ //signal, round, userChoice, signalList, choiceList, agentWins, agentSignals, allUserChoices
			if (expID == 0){
				choice = this.trialBehaviour(signal, round);
			} else if (expID == 1){
				choice = this.fakeHuman(signal, round, userChoice, signalList, choiceList, agentWins, agentSignals, totalSignals, allUserChoices);
			} else if (expID == 2){
				choice = this.condTwoFirst(signal, round);
			} else if (expID == 3){
				choice = this.condThreeFirst(signal, round, userChoice);
			} else if (expID == 4){
				choice = this.condTwoSecond(signal, round, userChoice);
			} else if (expID == 5){
				choice = this.condThreeSecond(signal, round, userChoice);
			}

			return choice;
		}
	

	this.cooperate =
		function(signal, round){
			if (round % 2 == 0) { //user signalled
        		choice = (signal+1)%24;
    		} else { //agent signalled
        		choice = signal;
   			}

    		return choice
		}

	this.deceive =
		function(signal, round){
			if (round % 2 == 0) { //user signalled
        		choice = (signal + 3) % 24;
    		} else { //agent signalled
        		choice = (signal + 2) % 24;
   			}

    		return choice
		}

	this.cheat = 
	//agent wins definitely
		function(userChoice){
			choice = (userChoice + 1) % 24;
			return choice;
		}

	this.noWinner = //TOP: changed to + 3 always, loop if 21/22/23
		function(signal, round, userChoice){
				if(userChoice == 23){
					choice = 2;
				} else if (userChoice == 22){
					choice = 3;
				}
				else if (userChoice == 21){
					choice = 4;
			} else if (userChoice < 21) {
					choice = (userChoice + 3)%24;
				}
			return choice;
		}

	this.trialBehaviour =
	//only cooperative
		function(signal, round){
			choice = this.cooperate(signal, round);
			
			return choice;
		}

	this.condTwoFirst = 
	//only cooperative
		function(signal, round){
			choice = this.cooperate(signal, round);
			
			return choice;
		}

	this.condThreeFirst = 
	//[1 x D][14 x C][5 x D]
		function(signal, round, userChoice){
			if (round == 1){
				choice = this.deceive(signal, round);
			} else if (round > 1 && round < 16){
				choice = this.cooperate(signal, round);
			} else if (round == 16){
				choice = this.cheat(userChoice);
			} else if (round == 17){
				choice = this.deceive(signal, round);
			} else if (round == 18){
				choice = this.cheat(userChoice);
			} else if (round == 19 || round == 20){
				choice = this.noWinner(signal, round, userChoice);
			}

			return choice;
		}

	this.condTwoSecond = 
	//[1 x D][19 x C]
		function(signal, round, userChoice){
			if (round == 1){
				choice = this.cheat(userChoice);
			} else {
				choice = this.cooperate(signal, round);
			}

			return choice;
		}

	this.condThreeSecond =
	//[15 x C][5 x D]
		function(signal, round, userChoice){
			if (round >= 1 && round < 16){
				choice = this.cooperate(signal, round);
			} else if (round == 16){
				choice = this.cheat(userChoice);
			} else if (round == 17){
				choice = this.deceive(signal, round);
			} else if (round == 18){
				choice = this.cheat(userChoice);
			} else if (round == 19 || round == 20){
				choice = this.noWinner(signal, round, userChoice);
			}

			return choice;
		}	

	this.countCoopRounds =
		function(signalList, choiceList, round, agentSignals, totalSignals, allUserChoices){ //signalList, choiceList, round, agentSignals, totalSignals, allUserChoices
			var length = totalSignals.length;
			var coopRounds =[];
			var count = 0;
			if(round < 3){ //TOP: changed to 3
				for(var i = 0; i < length; i++){ //0
					if(i%2==0 && (totalSignals[i] + 1)%24 == allUserChoices[i]){
						coopRounds.push(1);
					} else if(i%2==1 && totalSignals[i] == allUserChoices[i]){
						coopRounds.push(1);
					} else {
						coopRounds.push(0);
					}
				}
			} else if(round >= 3){  //TOP: changed to 3
				for(var i = length - 3; i < length; i++){  //TOP: changed to 3
					if(i%2==0 && (totalSignals[i] + 1)%24 == allUserChoices[i]){
						coopRounds.push(1);
					} else if(i%2==1 && totalSignals[i] == allUserChoices[i]){
						coopRounds.push(1);
					} else {
						coopRounds.push(0);
					}
				}
			}
			for(var j = 0; j < coopRounds.length - 1; j++){ //TOP: only look at previous rounds for coop! (-1 wasn't present)
				if(coopRounds[j] == 1){  
					count++;
				}
			}


			return count;
		}

	this.calculateProb = //calculates probability that agent plays cooperatively
		function(round, signalList, choiceList, agentSignals, totalSignals, allUserChoices){ //round, signalList, choiceList, agentSignals, totalSignals, allUserChoices
			var prob = 0;
			var count = this.countCoopRounds(signalList, choiceList, round, agentSignals, totalSignals, allUserChoices);

			//calculates probability that agent plays cooperatively this round (edit, see Top comments(?))

			if (round < 3) {  //TOP: changed to 3
				prob = count / (round - 1);
			} else {
				prob = count / 3;   //TOP: changed to 3
			}

			return prob;
		}

	this.fakeHumanDeceive =
		function(signal, round, userChoice, agentWins, random){
			// if the agent hasn't won for 4 consective rounds, it cheats 
			var losses = 0;
			if(round > 3){
				for(i = round - 4; i < round - 1; i++){
					if(agentWins[i] == 0){
						losses++;
						console.log("losses: " + losses);
					}
				}
			}
			

			if(losses >= 3 && signal < 14 && userChoice < (signal+10)){
				choice = this.cheat(userChoice);
				console.log("cheat, < 14");
				return choice;
			} else if(losses >= 3 && signal >= 14 && (userChoice >= signal && userChoice < signal + 10) || (userChoice <= signal && userChoice < ((signal + 10) - 24))){
				choice = this.cheat(userChoice);
				console.log("cheat >= 14");
				return choice;
			}

			/*
			if(losses >= 4 && (userChoice < (signal + 10))){
				choice = this.cheat(userChoice);
				return choice;
			}*/

			console.log(" ")
			console.log("We are in fakeHumanDeceive")
			console.log("")
			winRate = this.calculateWinRate(totalSignals, choiceList, agentSignals, round) //TOP: added line
			winRate += 1 //TOP: added
			console.log("Difference in opponent cooperative choice and what would have you win last turn: " + winRate)
			curSignal = totalSignals[round - 1] //TOP: added
			console.log("Current signal: " + curSignal)
			if(round % 2 == 0){ //user signalled THIS turn
				console.log("User signaled this turn")
				curCoopChoice = curSignal //TOP: added
				console.log("curCoopChoice: " + curCoopChoice)
				winningChoice = (curCoopChoice + winRate) % 24 //TOP: added
				//winningChoice = (signalList[signalList.length - 1] + winRate - 1) % 24; //TOP: signalList - totalSignals, replaced function call with winRate
				return winningChoice;
			} else {
				console.log("Agent signaled this turn")
				curCoopChoice = curSignal + 1//TOP: added
				console.log("curCoopChoice: " + curCoopChoice)
				winningChoice = (curCoopChoice + winRate) % 24 //TOP: added
				//winningChoice = ((totalSignals[totalSignals.length - 1] + 1) + winRate+ 1) % 24; //TOP: agentSignals - totalSignals, replaced function call with winRate
				return winningChoice;
			}
			/*if(random < 0.6){
				choice = this.deceive(signal, round);
				return choice;
			} else {
				choice = this.deceive(signal, round) + 2;
				return choice;
			}*/
		}

	this.calculateWinRate =
		function(totalSignals, choiceList, agentSignals, round){ //TOP: signalList to  totalSignals
			opponentActualChoiceLastRound = choiceList[round-2];//TOP: round instead of choiceList.length, -2 instead of -1
			console.log(" ")
			console.log("We are in calculateWinRate")
			console.log("Input:")
			console.log("totalSignals: " + totalSignals)
			console.log("choiceList: " + choiceList)
			console.log("agentSignals: " + agentSignals)
			console.log("roundL " + round)
			console.log("opponentActualChoiceLastRound: " + opponentActualChoiceLastRound)
			if((round - 1) % 2 == 0){ //user signalled
			console.log("User signalled last round")
				opponentCoopChoiceLastRound = totalSignals[round - 2]; //TOP: totalSignals, round - 2
			} else {
				console.log("Agent signalled last round")
				opponentCoopChoiceLastRound = (totalSignals[round - 2] + 1) % 24; //TOP: changed agentsignals to totalSignals, added % 24, round -2
			}
			console.log("Cooperative choice for opponent last round: " + opponentCoopChoiceLastRound)

			if(opponentCoopChoiceLastRound <= opponentActualChoiceLastRound){
				winRateLastRound = opponentActualChoiceLastRound - opponentCoopChoiceLastRound;
			} else {
				winRateLastRound = opponentCoopChoiceLastRound - opponentActualChoiceLastRound;
			}
			
			console.log("Difference between opponent cooperative and actual choice last round: " + winRateLastRound)
			

			return winRateLastRound;
		}

	this.fakeHuman =
		function(signal, round, userChoice, signalList, choiceList, agentWins, agentSignals, totalSignals, allUserChoices){
			console.log(" ")
			console.log("We are in fakeHuman")
			console.log("Input:")
			console.log("choiceList: " + choiceList)
			if(round == 1){
				console.log("Cooperate on first round")
				choice = this.cooperate(signal, round);
			} else {
				prob = this.calculateProb(round, signalList, choiceList, agentSignals, totalSignals, allUserChoices);
				random = Math.random();
				if(random < prob){
					console.log("Cooperate on subsequent round")
					choice = this.cooperate(signal, round);
				} else {
					console.log("Deceive")
					choice = this.fakeHumanDeceive(signal, round, userChoice, agentWins, random);
				}
			}
			
			return choice;
		}
}














