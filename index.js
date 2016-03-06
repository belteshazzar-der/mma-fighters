mma = require('./lib/mma');
lineByLineReader = require('line-by-line');
fs = require('fs');

outputARFF = './data/mma-fighters.arff';
headerARFF = './data/arff-header.txt';

//Write out ARFF Header
fs.writeFile(outputARFF, "", function(err) {

    if(err) {
        return console.log(err);
    }

    console.log("\nWrote ARFF header...\n");
}); 

lr = new lineByLineReader('./data/test.txt');

lr.on('error', function (err) {
	// 'err' contains error object
	console.log(err);
});

lr.on('line', function (line) {

	lr.pause();

	// 'line' contains the current line without the trailing newline character.
	mma.fighter(line, function(data) {

		//This is were the parsing takes place!
		//Create a string with all the relevant features
		entry = data.name + ',';
		entry += data.age + ',';
		entry += data.nationality + ',';
		entry += data.weight + ',';
		entry += data.wins.total + ',';
		entry += data.losses.total + ',';
		entry += data.wins.knockouts + ',';
		entry += data.wins.submissions + '\n';

	    fs.appendFile(outputARFF, entry, function(err) {
		    
		    if(err) {
		        return console.log(err);
		    }
		}); 
 	});

 	lr.resume();
});

lr.on('end', function () {
	// All lines are read, file is closed now.
	console.log('==================================================================');
	console.log('Writing all data to ' + outputARFF);
	console.log('==================================================================\n');
});

