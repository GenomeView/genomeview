<?php
$applet = $_GET['applet']=="true";
$webstart = !$applet;
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header('Pragma: no-cache');

if ($webstart){
  header('Content-Type: application/x-java-jnlp-file');
  header("Content-Disposition: attachment; filename=GenomeView.jnlp");
}

echo('<?xml version="1.0" encoding="utf-8"?>');

$siteroot = 'http://' . $_SERVER["HTTP_HOST"] . str_replace(basename($_SERVER["PHP_SELF"]), "", $_SERVER["PHP_SELF"]);
//$siteroot="http://sourceforge.net/projects/genomeview/files/webstart/";
//$siteroot="http://downloads.sourceforge.net/project/genomeview/webstart/";
//$siteroot="http://localhost/";


$param = html_entity_decode($_SERVER["QUERY_STRING"]);

$this_file = "launch.jnlp.php";
if (!empty($param)){
  $this_file.="?".$param;
}

$jars[]="genomeview-1097i3.jar";
$jars[]="jannot-1097.jar";
$jars[]="jargs.jar";
$jars[]="commons-logging.jar";
$jars[]="sam-938.jar";
$jars[]="ajt-2.4.jar";
$jars[]="collections-1.0.jar";

?>

<jnlp <?php if ($webstart):   ?>
  spec="1.0+"
  codebase="<?=$siteroot?>"
  <?php endif; ?>>
<information>
  <title>GenomeView</title>
  <vendor>Thomas Abeel</vendor>
  <homepage href="http://genomeview.sf.net" />
  <icon href="http://broadinstitute.org/software/genomeview/gv2.png" width="47" height="47"  />
  <description>GenomeView: a next-generation genome browser and editor</description>
</information>
<security>
  <all-permissions />
</security>
<update check="always" policy="always" />
<resources>
 <j2se version="1.6+" java-vm-args="-ea" initial-heap-size="128M" max-heap-size="1000M"/> 
  	 <?php
  foreach ($jars as $jar_file) {
     echo "<jar href=\"" . $siteroot . "" . $jar_file . "\" />\n";
  }
?>
  
</resources>

<?php
//strip classic url parameters
if (!empty($param)){
  $param_location = strrpos($param, "&")+1;
  $param = substr($param, $param_location);
}
?>

<?php if ($webstart): ?>
<application-desc main-class="net.sf.genomeview.gui.GenomeView">
<?php 
if (!empty($param)){
  $param=str_replace("%25","%",$param);
  $params=explode("%20",$param);
  foreach($params as $single){
    $single=str_replace("%3f","?",$single);
    echo "<argument>". $single."</argument>";
  }
} 
?>
</application-desc>
<?php endif; ?>


<?php if ($applet): ?>
<applet-desc name="GenomeView Demo Applet"
      main-class="net.sf.genomeview.gui.GVApplet"
      width="800"
      height="500">   
    <param name="image" value="gv_logo_ani.gif"/>
    <param name="boxborder" value="false" />
    <param name="centerimage" value="true" />
  </applet-desc>

<?php endif; ?>

</jnlp>
