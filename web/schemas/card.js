var mongoose = require('mongoose');
var Card = new mongoose.Schema({
    cardtype: String,
    cardnumber: String,
    cadrexpiration:String
});
module.exports = mongoose.model('Card', Card);