let express = require('express');
let router = express.Router();
let Card = require('../schemas/card');

router.post("/new-card",function (req, res) {
    let newCard = new Card({
        cardtype: req.body.cardtype,
        cardnumber: req.body.cardnumber,
        cardexpiration:req.body.cardexpiration
    });
    card.save().then(function () {
        res.status(200).json({message: 'Success'});
    });
});

module.exports = router;