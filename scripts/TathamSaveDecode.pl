#! /usr/bin/perl
# this script takes three command line arguments:
# a width, a height, and a Simon Tatham save string for a gridded puzzle
# and will print out the save string
# 
# a Simon Tatham save string contains letters and numbers
# a number is a literal number a a particular site
# a letter is the number of blank spaces
# 
# starting from upper left going linewise, then down.

die("bad command line") unless @ARGV == 3;
$width = $ARGV[0];
$height = $ARGV[1];
$stss = $ARGV[2];
$dec = "";

sub nol
{
	my ($char) = @_;
	return ord($char) - ord('a') + 1;
}


print "Raw: $stss\n";

for my $ch (split //,$stss)
{
	if ($ch =~ /[a-z]/)
	{
		$dec .= "." x nol($ch);
	}
	else
	{
		$dec .= $ch;
	}
}

print $dec,"\n";

for (my $i = 0 ; $i < length($dec) ; ++$i)
{
	print "|\n" if ($i % $width == 0);
	print substr($dec,$i,1);
}
print "|\n";

