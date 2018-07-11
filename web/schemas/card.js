var mongoose = require('mongoose');
var Card = new mongoose.Schema({
    cardtype: String,
    cardnumber: String,
    cardexpiration:String,
    file:String

});
module.exports = mongoose.model('Card', Card);