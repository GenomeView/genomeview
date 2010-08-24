<html>
<head>
<title>GenomeView Applet</title>
</head>
<style>
applet {
	border: solid 1px black;
	-moz-box-shadow: 0 0 10px rgba(50, 50, 50, 0.8);
	-webkit-box-shadow: 0 0 10px rgba(50, 50, 50, 0.8);
	box-shadow: 0 0 10px rgba(50, 50, 50, 0.8);
	background: url('logo.png') no-repeat center;
}
</style>
<body>
<h1>GenomeView: C. Elegans</h1>

<?php
$pageURL = 'http';
if (isset($_SERVER["HTTPS"])&&$_SERVER["HTTPS"] == "on") {$pageURL .= "s";}
$pageURL .= "://";
$baseurl = $pageURL .$_SERVER["SERVER_NAME"].$_SERVER["REQUEST_URI"];
$baseurl =substr($baseurl, 0, strripos($baseurl, "/") + 1);
$basedir = substr($_SERVER["SCRIPT_FILENAME"], 0, strripos($_SERVER["SCRIPT_FILENAME"], "/") + 1);

echo "BI".$baseurl.'/BI';
echo 'BD'.$basedir.'/BD';

$tracks = dirList($basedir . ".");
?>

<?php

$all = "";
foreach ($tracks as $track) {
	$all .= " ".$baseurl. $track;
}
echo $all;



?>
<script src="http://www.java.com/js/deployJava.js"></script>
<script> 
	var attributes = { code:'net.sf.genomeview.gui.GVApplet',  width:800, height:500} ; 
    var parameters = {jnlp_href: "http://genomeview.sf.net/start.jnlp.php?applet=true",
	extra: "<?php echo $all;?>"} ; 
    deployJava.runApplet(attributes, parameters, '1.6'); 
</script>


</body>
</html>

<?php
function endsWith( $str, $sub ) {
	return ( substr( $str, strlen( $str ) - strlen( $sub ) ) == $sub );
}
function dirList($directory) {

	// create an array to hold directory list
	$results = array ();

	// create a handler for the directory
	$handler = opendir($directory);

	// keep going until all files in directory have been read
	while ($file = readdir($handler)) {
		/*
		 * Files that will be excluded:
		 * this directory (.) and the parent (..)
		 * index.php
		 * .htaccess
		 * All *.bam files, the bai will be included
		 */
		if ($file != '.' && $file != '..' && $file != '.htaccess'&&!endsWith($file,".html")&&!endsWith($file,".php")&&!endsWith($file,".bam")&& $file != 'config.local'&& $file != 'index.php')
		$results[] = $file;
	}

	// tidy up: close the handler
	closedir($handler);

	// done!
	return $results;

}
?>



