<?php
/* PHP 4.x compatibility */
if(!function_exists("stripos")){
    function stripos(  $str, $needle, $offset = 0  ){
        return strpos(  strtolower( $str ), strtolower( $needle ), $offset  );
    }/* endfunction stripos */
}/* endfunction exists stripos */

if(!function_exists("strripos")){
    function strripos(  $haystack, $needle, $offset = 0  ) {
        if(  !is_string( $needle )  )$needle = chr(  intval( $needle )  );
        if(  $offset < 0  ){
            $temp_cut = strrev(  substr( $haystack, 0, abs($offset) )  );
        }
        else{
            $temp_cut = strrev(    substr(   $haystack, 0, max(  ( strlen($haystack) - $offset ), 0  ) 

  )    );
        }
        if(   (  $found = stripos( $temp_cut, strrev($needle) )  ) === FALSE   )return FALSE;
        $pos = (   strlen(  $haystack  ) - (  $found + $offset + strlen( $needle )  )   );
        return $pos;
    }/* endfunction strripos */
}/* endfunction exists strripos */
?>


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
<?php
$pageURL = 'http';
if (isset($_SERVER["HTTPS"])&&$_SERVER["HTTPS"] == "on") {$pageURL .= "s";}
$pageURL .= "://";
$baseurl = $pageURL .$_SERVER["SERVER_NAME"].$_SERVER["REQUEST_URI"];
$baseurl =substr($baseurl, 0, strripos($baseurl, "/") + 1);
$basedir = substr($_SERVER["SCRIPT_FILENAME"], 0, strripos($_SERVER["SCRIPT_FILENAME"], "/") + 1);

$tracks = dirList($basedir . ".");
?>

<?php

$all = "";
foreach ($tracks as $track) {
	$all .= " ".$baseurl. $track;
}
?>

<script src="http://www.java.com/js/deployJava.js"></script>
<script src="http://genomeview.org/start/genomeview.js"></script>
<script> 
   	var gv_extra="<?php echo $all;?>";
	startGV(null,null,null,gv_extra,800,600); 
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
		if (!is_dir($file)&&$file != '.' && $file != '..' && $file != '.htaccess'&&!endsWith($file,".html")&&!endsWith($file,".php")&&!endsWith($file,".tbi")&&!endsWith($file,".bam")&& $file != 'config.local'&& $file != 'index.php')
		$results[] = $file;
	}

	// tidy up: close the handler
	closedir($handler);

	// done!
	return $results;

}
?>



