exports.showMain = function showMain(req, res) {
  res.render("indexs", {
    name: "JK",
    names: ["JK", "JOHN", "DAVID"],
  });
};
