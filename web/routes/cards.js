let express = require('express');
let router = express.Router();
let Card = require('../schemas/card');
let Pass = require('../schemas/pass');
const bcrypt = require('bcrypt-nodejs');


router.post("/new-card",function (req, res) {
    let newCard = new Card({
        cardtype: req.body.cardtype,
        cardnumber: req.body.cardnumber,
        cardexpiration:req.body.cardexpiration
    });
    newCard.save().then(function () {
        res.status(200).json({message: 'Success'});
    });
});


router.get("/get-cards",function (req, res) {
    Pass.findOne({},function (err, pwd) {
        if (err) {
            console.log(err);
            res.status(500).json(err);
        }
        else {
            if (bcrypt.compareSync("erandamirshay", pwd.password)) {
                Card.find({}, function (err, cards) {
                    if (err) {
                        console.log(err);
                        res.status(500).json(err);
                    }
                    else {
                        res.status(200).json({ans: cards, auth: true});
                    }
                });
            }
            else {
                res.status(404).json({auth: false});
            }
        }
        });
});

module.exports = router;