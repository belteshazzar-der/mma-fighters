var mma = require('./lib/mma');
var mongoose = require('mongoose');
var lineByLineReader = require('line-by-line');
var config = require('./config'); // get our config file
var Fighter = require('./models/fighter');

mongoose.connect(config.database); // connect to database

lr = new lineByLineReader('./data/small.txt');

console.log("\nStarting to build database!\n\n");

lr.on('error', function (err) {
	// 'err' contains error object
	console.log(err);
});

lr.on('line', function (line) {

	lr.pause();

	// 'line' contains the current line without the trailing newline character.
	mma.fighter(line, function(data) {

		var fighter = new Fighter();

		//This is were the parsing takes place!
		fighter.name = data.name;
		fighter.weight = data.weight;
		fighter.weight_class = data.weight_class;
		fighter.age = data.age;
		fighter.height = data.height;
		fighter.wins_total = data.wins.total;
		fighter.wins_ko = data.wins.knockouts;
		fighter.wins_to = data.wins.submissions;
		fighter.wins_decision = data.wins.decisions;
		fighter.wins_other = data.wins.others;
		fighter.losses_total = data.losses.total;
		fighter.losses_ko = data.losses.knockouts;
		fighter.losses_to = data.losses.submissions;
		fighter.losses_decision = data.losses.decisions;
		fighter.losses_other = data.losses.others;
		fighter.strikes_attempted = data.strikes.attempted;
		fighter.strikes_successful = data.strikes.successful;
		fighter.strikes_standing = data.strikes.standing;
		fighter.strikes_clinch = data.strikes.clinch;
		fighter.strikes_ground = data.strikes.ground;
		fighter.takedowns_attempted = data.takedowns.attempted;
		fighter.takedowns_successful = data.takedowns.successful;
		fighter.takedowns_submissions = data.takedowns.submissions;
		fighter.takedowns_passes = data.takedowns.passes;
		fighter.takedowns_sweeps = data.takedowns.sweeps;
		fighter.fights = [];

		for (var i in data.fights) {

			var fight = data.fights[i];
			var newFight = {opponent: fight.opponent, result: fight.result};
			fighter.fights.push(newFight);
		}

		fighter.save(function(err) {
            if (err){
                console.log('Couldn\'t save fighter JSON! ' + line + ' ' + err);  
                return;
            }

            console.log(fighter.name);
	    });
 	});

 	lr.resume();
});

lr.on('end', function () {
	// All lines are read, file is closed now.
});

