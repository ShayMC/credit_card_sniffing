var mongoose = require('mongoose');
var DT = new mongoose.Schema({
    data: [],
    target: String,
    features:[]
});
module.exports = mongoose.model('DT', DT);