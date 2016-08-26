package io.github.jhg0.vATISFetch;

import io.github.jhg0.vATISFetch.Handlers.ATISHandler;
import io.github.jhg0.vATISFetch.Handlers.ConfigHandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings({"UnusedDeclaration"})
public class Display
{

    private static JFrame frame;
    private JPanel panel;

    private JComboBox configSelection;

    private JTextArea generalFetch;
    private JTextArea notamFetch;

    private JButton fetchButton;
    private JButton closeButton;
    private JButton importButton;

    private SwingWorker worker;

    private ATISHandler atisHandler;
    private ConfigHandler configHandler;

    public Display()
    {
        atisHandler = new ATISHandler();
        configHandler = new ConfigHandler();

        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (worker != null) worker.cancel(true);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });

        configSelection.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (configSelection.getSelectedItem() != null)
                    configHandler.parseConfig(configSelection.getSelectedItem().toString(), generalFetch);
            }
        });

        fetchButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                worker = new SwingWorker<Void, Void>()
                {
                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        atisHandler.fetchATIS(configSelection.getSelectedItem().toString(), notamFetch);
                        String[] out = atisHandler.mergeATIS(configHandler.getConfig());
                        generalFetch.setText(out[0]);
                        notamFetch.setText(out[1]);
                        return null;
                    }

                    @Override
                    protected void done()
                    {
                    }
                };
                worker.execute();
            }
        });

        importButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                worker = new SwingWorker<Void, Void>()
                {
                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        importConfigs();
                        return null;
                    }

                    @Override
                    protected void done()
                    {
                    }
                };
                worker.execute();
            }
        });

    }

    public void run()
    {
        frame = new JFrame("vATISFetch");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        configHandler.initConfigSelection(configSelection);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void importConfigs()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.home") + File.separator + "Downloads"));
        chooser.setFileFilter(new FileNameExtensionFilter("JSON File (*.json)", "json"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(true);
        chooser.showOpenDialog(frame);
        File[] files = chooser.getSelectedFiles();

        File configDirectory = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\vATIS\\Fetch");
        if (!configDirectory.exists()) configDirectory.mkdir();
        try
        {
            for (File f : files)
                Files.move(Paths.get(f.getPath()), Paths.get(configDirectory.getPath() + File.separator + f.getName()));
        } catch (Exception ignored)
        {
        }
        configHandler.initConfigSelection(configSelection);
    }

}