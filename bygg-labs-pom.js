const fs = require("node:fs");

const file = fs
  .readFileSync("./pom.xml", "utf8")

  // Finn neste scope-tag etter h2, som forhåpentligvis tilhører h2-avhengighet...

  .replace(/(?<=<artifactId>h2<\/artifactId>.*?)<scope>.*?<\/scope>/s, "")

  // Samme for wiremock...

  .replace(
    /(?<=<artifactId>wiremock-standalone<\/artifactId>.*?)<scope>.*?<\/scope>/s,
    ""
  );

// Skriv labs-pom:

const output = fs.createWriteStream("./labs-pom.xml");
output.write(file);
output.end();
