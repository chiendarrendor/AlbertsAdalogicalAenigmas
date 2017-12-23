

open( my $fd, "<", "addenda5.txt") || die("Can't open file\n");

my $currow = 0;
my %cells;
my %maps;

while(<$fd>)
{
	chomp;
	if (length($_) == 12)
	{
		for (my $i = 0 ; $i < 12 ; ++$i)
		{
			$cells{$i,$currow} = substr($_,$i,1);
		}
		++$currow;
		next;
	}
	my @ents = split(/\s+/,$_);
	if (@ents == 2)
	{
		$maps{$ents[0]} = $ents[1];
	}
}

close ($fd);

for my $key (keys %cells)
{
	if (exists $maps{$cells{$key}})
	{
		$cells{$key} = $maps{$cells{$key}} ;
	}
	elsif ($cells{$key} =~ /^[A-Z]$/)
	{
		$cells{$key} = '.'
	}
}

for (my $y = 0 ; $y < 10 ; ++$y)
{
	for (my $x = 0 ; $x < 12 ; ++$x)
	{
		print $cells{$x,$y};
	}
	print "\n";
}


my $gapctr = 0;
my $result = "";

sub AddToGap
{
	return unless $gapctr > 0;
	my $offset = $gapctr - 1;
	$gapctr = 0;
	my $gapchar = chr(ord('a') + $offset);
	$result .= $gapchar;
}




for (my $y = 0 ; $y < 10 ; ++$y)
{
	for (my $x = 0 ; $x < 12 ; ++$x)
	{	
		my $char = $cells{$x,$y};
		if ($char eq '.')
		{
			++$gapctr;
		}
		else
		{
			AddToGap();
			$result .= $char;
		}
	}
}
AddToGap();
	
print length($result),":",$result,"\n";

	
	
	
	
	
	
	
	
	
	
	
	
	