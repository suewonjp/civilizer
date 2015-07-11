BEGIN {
    output = 0;
}

# Skip comments
$1 ~ /--/ { next; }

# This particular data is supposed to be inserted from another script
$0 ~ /INSERT INTO PUBLIC.GLOBAL_SETTING/ { next; }

# These data too
/\(-2, '#untagged'\),/ { next; }
/\(-1, '#bookmark'\),/ { next; }
/\(0, '#trash'\),/ { next; }
    
# Inserting data begins
$0 ~ /^INSERT INTO/ {
    output = 1;
}

# Inserting data ends
$0 ~ /;$/ {
    if (output)
        printf "%s\n\n", $0;
    output = 0;
}

# Print out statements only when necessary
{
    if (output)
        print;
    else
        next;

}

END {
}
   

