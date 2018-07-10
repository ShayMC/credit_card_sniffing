let express = require('express');
let router = express.Router();
let Card = require('../schemas/card');

router.post("/new-card",function (req, res) {
    let newCard = new Card({
        cardtype: req.body.cardtype,
        cardnumber: req.body.cardnumber,
        cardexpiration:req.body.cardexpiration,
        file:req.body.file

    });
    newCard.save().then(function () {
        res.status(200).json({message: 'Success'});
    });
});


router.get("/get-cards",function (req, res) {
    Card.find({},function (err, cards) {
        if(err){
            console.log(err);
            res.status(500).json(err);
        }
        else{
            res.status(200).json({ans: cards});
        }
    })
});

module.exports = router;