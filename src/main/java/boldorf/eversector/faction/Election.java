package boldorf.eversector.faction;

import boldorf.apwt.glyphs.ColorString;
import boldorf.eversector.ships.Reputation;
import boldorf.eversector.ships.Ship;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static boldorf.eversector.Main.player;

/**
 *
 */
public class Election
{
    /**
     * The number of candidates that are selected for an election.
     */
    public static final int CANDIDATES = 4;

    private Faction faction;
    private List<Ship> candidates;
    private List<Integer> votes;
    private boolean emergency;

    public Election(Faction faction, boolean emergency)
    {
        this.faction = faction;
        candidates = new ArrayList<>(CANDIDATES);
        votes = new ArrayList<>(CANDIDATES);
        this.emergency = emergency;
    }

    public Faction getFaction()
    {return faction;}

    public List<Ship> getCandidates()
    {return candidates;}

    public List<Integer> getVotes()
    {return votes;}

    public boolean isEmergency()
    {return emergency;}

    public int getMinimumReputation()
    {
        int minRep = candidates.get(0).getReputation(faction).get();
        for (Ship candidate : candidates)
        {
            minRep = Math.min(minRep, candidate.getReputation(faction).get());
        }
        return minRep;
    }

    public ColorString getDescription()
    {
        if (emergency)
        {
            return new ColorString("The ").add(faction).add(" is holding an emergency election for leader.");
        }

        return new ColorString("The scheduled leader election for the ").add(faction).add(" has arrived.");
    }

    public Ship electLeader()
    {
        findCandidates();

        // This gives higher-reputation candidates a slight bias among
        // voters
        candidates.sort(Comparator.reverseOrder());
        gatherVotes();
        Ship winner = getWinner();
        lowerWinnerReputation(winner);
        return winner;
    }

    public void findCandidates()
    {
        int minRep = 0;

        for (Ship ship : faction.getGalaxy().getShips())
        {
            if (faction == ship.getFaction() && (ship.getReputation(faction).get() > minRep) ||
                candidates.size() < CANDIDATES)
            {
                candidates.add(ship);

                if (candidates.size() > CANDIDATES)
                {
                    candidates.sort(Comparator.naturalOrder());
                    candidates.remove(0);
                }

                int lowestRep = ship.getReputation(faction).get();

                for (Ship candidate : candidates)
                {
                    if (candidate.getReputation(faction).get() < lowestRep)
                    {
                        lowestRep = candidate.getReputation(faction).get();
                    }
                }

                minRep = lowestRep;
            }
        }

        candidates.sort(Comparator.reverseOrder());
    }

    public void gatherVotes()
    {
        // Fill the vote list with 0s as a starting point
        for (Ship candidate : candidates)
        {
            votes.add(0);
        }

        for (Ship ship : faction.getGalaxy().getShips())
        {
            if (faction == ship.getFaction() && !candidates.contains(ship))
            {
                Ship vote = ship.getAI().vote(candidates);
                int index = candidates.indexOf(vote);
                votes.set(index, votes.get(index) + 1);
            }
        }
    }

    public Ship getWinner()
    {
        int winnerIndex = 0;
        int highestVotes = 0;

        for (int i = 0; i < votes.size(); i++)
        {
            if (votes.get(i) > highestVotes)
            {
                winnerIndex = i;
                highestVotes = votes.get(i);
            }
        }

        return candidates.get(winnerIndex);
    }

    public boolean isReelected(Ship winner)
    {return faction.isLeader(winner);}

    public void lowerWinnerReputation(Ship winner)
    {
        if (isReelected(winner))
        {
            winner.changeReputation(faction, Reputation.REELECTION);
        }
    }

    public void addPlayer()
    {
        candidates.sort(Comparator.naturalOrder());
        candidates.remove(0);
        candidates.add(player);
        candidates.sort(Comparator.reverseOrder());
    }

    public void addVote(String candidate)
    {
        for (int i = 0; i < candidates.size(); i++)
        {
            if (candidates.get(i).toString().equals(candidate))
            {
                votes.set(i, votes.get(i) + 1);
                return;
            }
        }
    }
}