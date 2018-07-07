let express = require('express');
let router = express.Router();
const DecisionTree = require('decision-tree');
const Stat = require('../schemas/stat');
const DT = require('../schemas/dt');

router.post("/process",function (req, res) {

    console.log("learning " + req.query.name + ".csv");

    Stat.find({},function (err, all) {
        if(err){
            console.log(err);
            res.status(500).json(err);
        }
        else{
            let training_data = [];
            all.forEach(function (entry) {
                training_data.push(entry._doc);
            });
            let class_name = "race";
            let features = ["intent","sex","education","place"];
            let dt = new DecisionTree(training_data, class_name, features);


            let tree = new DT({
                data: dt.data,
                target: dt.target,
                features:dt.features
            });
            tree.save().then(function () {
                res.status(200).json({message:"Success"});
            })

        }
    });
});


router.post('/predict',function (req, res) {

    console.log("predicting...");

    DT.findOne({},function (err, tree) {
        if(err){
            console.log(err);
            res.status(500).json(err);
        }
        else{
            let dt = new DecisionTree(tree.data, tree.target, tree.features);
            let predicted_class = dt.predict({
                intent: req.query.intent,
                sex: req.query.sex,
                education: req.query.education,
                place:req.query.place
            });

            res.status(200).json({ans:predicted_class});
            console.log(predicted_class);
        }
    });
});



module.exports = router;