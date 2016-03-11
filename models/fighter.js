// get an instance of mongoose and mongoose.Schema
var mongoose = require('mongoose');
var Schema = mongoose.Schema;

// set up a mongoose model and pass it using module.exports
module.exports = mongoose.model('Fighter', new Schema({ 
	name: {type: String, required: true},
    weight: {type: String, required: true},
    weight_class: {type: String, required: true}, 
    age: {type: String, required: true},
    height: {type: String, required: true},
    wins_total: {type: String, required: true},
    wins_ko: {type: String, required: true},
    wins_to: {type: String, required: true},
    wins_decision: {type: String, required: true},
    wins_other: {type: String, required: true},
    losses_total: {type: String, required: true},
    losses_ko: {type: String, required: true},
    losses_to: {type: String, required: true},
    losses_decision: {type: String, required: true},
    losses_other: {type: String, required: true},
    strikes_attempted: {type: String, required: true},	
    strikes_successful: {type: String, required: true},
    strikes_standing: {type: String, required: true},
    strikes_clinch: {type: String, required: true},
    strikes_ground: {type: String, required: true},
    takedowns_attempted: {type: String, required: true},
    takedowns_successful: {type: String, required: true},
    takedowns_submissions: {type: String, required: true},
    takedowns_passes: {type: String, required: true},
    takedowns_sweeps: {type: String, required: true},
    fights: {type: [{opponent: String, result: String}], required: true}
}));