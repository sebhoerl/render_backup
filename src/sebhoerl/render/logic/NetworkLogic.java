package sebhoerl.render.logic;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Iterator;

public class NetworkLogic {
    double now = 0.0;
        
    LinkedList<LinkPassage> passages = new LinkedList<LinkPassage>();
    PriorityQueue<LinkPassage> active = new PriorityQueue<LinkPassage>(new Comparator<LinkPassage>() {
        @Override
        public int compare(LinkPassage o1, LinkPassage o2) {
            return Double.compare(o1.getEndTime(), o2.getEndTime());
        }
    });
    
    Iterator<LinkPassage> iterator;
    LinkPassage upcoming;
    
    public void finish() {
        passages.sort(new Comparator<LinkPassage>() {
            @Override
            public int compare(LinkPassage o1, LinkPassage o2) {
                return Double.compare(o1.getStartTime(), o2.getStartTime());
            }
        });
        
        reset(0.0);
    }
    
    public void addPassage(LinkPassage passage) {
        passages.add(passage);
    }
    
    public void reset(double time) {
        active.clear();
        now = 0.0;
        
        iterator = passages.iterator();
        upcoming = iterator.hasNext() ? iterator.next() : null;
        
        advance(time);
    }
    
    public void advance(double dt) {
        now += dt;
        
        while (upcoming != null && upcoming.getStartTime() <= now) {
            if (upcoming.getEndTime() > now) {
                active.add(upcoming);
                upcoming.getVehicle().setActive(true);
            }
            
            upcoming = iterator.hasNext() ? iterator.next() : null;
        }
        
        while (!active.isEmpty() && active.peek().getEndTime() < now) {
            active.poll().getVehicle().setActive(false);
        }
        
        for (LinkPassage passage : active) {
            passage.advance(now);
        }
    }
}
