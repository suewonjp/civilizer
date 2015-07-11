BEGIN {
    RS=";\n";
    }

$1 ~ /--/ { next; }

$0 ~ /INSERT INTO PUBLIC.GLOBAL_SETTING/ { next; }

$0 ~ /INSERT INTO PUBLIC.TAG\(/ { 
    gsub(/\(-2, '#untagged'\),\n/, ""); 
    gsub(/\(-1, '#bookmark'\),\n/, ""); 
    gsub(/\(0, '#trash'\),\n/, ""); 
    printf "%s;\n", $0; 
    next;
    }
    
$0 ~ /INSERT INTO/ {
#$1 ~ /INSERT/ && $2 ~ /INTO/ {
   printf "%s;\n", $0; 
}

END {
    }

