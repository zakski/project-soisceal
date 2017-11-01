package alice.tuprologx.ide;

import java.util.EventListener;

/**
 * Listener for information to display in the console events
 */

interface InformationToDisplayListener
        extends EventListener {
    void onInformation(InformationToDisplayEvent e);
}