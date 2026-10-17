const express = require("express");
const app = express();
const port = 3101;

//
const main = require("./main.js");
app.set("view engine", "ejs");
app.get("/", (req, res) => {
  main.showMain(req, res);
});

app.listen(port, () => {
  console.log(`listen:  http://localhost:${port}`);
});
