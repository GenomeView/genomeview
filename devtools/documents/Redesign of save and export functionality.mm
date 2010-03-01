<map version="0.8.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1264598196639" ID="Freemind_Link_1647638470" MODIFIED="1264598317134" TEXT="Redesign of save and export functionality">
<node CREATED="1264598328685" ID="Freemind_Link_1463302878" MODIFIED="1264598527128" POSITION="right" TEXT="Export/save as...">
<node CREATED="1264598203032" ID="_" MODIFIED="1264598228072" TEXT="The goal of this would be to export (part) of the data that is currently loaded"/>
<node CREATED="1264598228369" ID="Freemind_Link_1195500817" MODIFIED="1264598251095" TEXT="Export of read-only files data: copy file to wherever the user wants it"/>
<node CREATED="1264598254831" ID="Freemind_Link_932363988" MODIFIED="1264598258129" TEXT="Export features">
<node COLOR="#338800" CREATED="1264598258957" ID="Freemind_Link_1411107503" MODIFIED="1264599627680" TEXT="by type"/>
<node COLOR="#338800" CREATED="1264599130462" ID="Freemind_Link_213165936" MODIFIED="1264599627352" TEXT="by entry"/>
<node COLOR="#338800" CREATED="1264598260489" ID="Freemind_Link_1083302524" MODIFIED="1264599626602" TEXT="by source"/>
</node>
</node>
<node CREATED="1264598324449" ID="Freemind_Link_107207558" MODIFIED="1264598326012" POSITION="right" TEXT="Save">
<node CREATED="1264598339001" ID="Freemind_Link_643284739" MODIFIED="1264598543993" TEXT="Save is only required for features, read-only stuff can&apos;t be saved"/>
<node COLOR="#ff0000" CREATED="1264598374654" ID="Freemind_Link_1398727997" MODIFIED="1264599654236" TEXT="Save to a fixed location determined at loading?">
<node CREATED="1264599472424" ID="Freemind_Link_1352576536" MODIFIED="1264599496182" TEXT="If everything is loaded from a single source this is easy"/>
<node CREATED="1264599496541" ID="Freemind_Link_176837135" MODIFIED="1264599504231" TEXT="What about extra loaded data?"/>
<node CREATED="1264599504450" ID="Freemind_Link_1287425710" MODIFIED="1264599511390" TEXT="What about multi-source instances?"/>
</node>
<node COLOR="#338800" CREATED="1264599573412" ID="Freemind_Link_1590236191" MODIFIED="1264599618646" TEXT="Save to the place where it came from">
<node CREATED="1264599580727" ID="Freemind_Link_752109395" MODIFIED="1264599584790" TEXT="This is the most logical way"/>
<node CREATED="1264599585134" ID="Freemind_Link_1691567275" MODIFIED="1264599616911" TEXT="But not the most efficient">
<edge WIDTH="thin"/>
</node>
</node>
<node CREATED="1264598450805" ID="Freemind_Link_1733641486" MODIFIED="1264599041650" TEXT="We may want a configuration option for which types are &apos;saveble&apos;"/>
<node CREATED="1264599071082" ID="Freemind_Link_243801040" MODIFIED="1264599097497" TEXT="What about files containing both read-only and features? Best example being EMBL files">
<node CREATED="1264599755599" ID="Freemind_Link_450495370" MODIFIED="1264599776185" TEXT="Load as read-only and RW component?"/>
<node CREATED="1264599776622" ID="Freemind_Link_671408605" MODIFIED="1264599782781" TEXT="Load as RW component?"/>
<node CREATED="1264599783046" ID="Freemind_Link_767936614" MODIFIED="1264599798333" TEXT="Load as RO component?">
<node CREATED="1264599804726" ID="Freemind_Link_1627805147" MODIFIED="1264599819387" TEXT="show message that changes to EMBL file cannot be saved"/>
</node>
</node>
<node CREATED="1264600121273" ID="Freemind_Link_349574801" MODIFIED="1264600147922" TEXT="Files which support tiling can never be saved because that would require the entire file to be present"/>
</node>
</node>
</map>
