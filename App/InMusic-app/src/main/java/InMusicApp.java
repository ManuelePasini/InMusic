import org.apache.commons.collections4.BidiMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class InMusicApp {
    private final static Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private final static ConnectionManager CM = ConnectionManager.getInstance();
    public static void main(String[] args) throws IOException {
        JTabbedPane menu = new JTabbedPane();
        JPanel preImagesPanel =  new JPanel();
        JPanel postImagesPanel =  new JPanel();
        JPanel songsPanel =  new JPanel();
        ButtonGroup preImagesButtonGroup = new ButtonGroup();
        ButtonGroup postImagesButtonGroup = new ButtonGroup();

        AtomicReference<String> locationNameString = new AtomicReference<>("");
        AtomicReference<String>  locationDescString = new AtomicReference<>("");
        AtomicReference<String>  oldLocation = new AtomicReference<>("");
        AtomicReference<String>  memoryNameString = new AtomicReference<>("");
        AtomicReference<String>  memoryDescString = new AtomicReference<>("");
        AtomicReference<String>  toDoNameString = new AtomicReference<>("");
        AtomicReference<String>  toDoDescString = new AtomicReference<>("");
        AtomicReference<String>  toDoActivityTypeString = new AtomicReference<>("");
        AtomicReference<String>  whileDoingNameString = new AtomicReference<>("");
        AtomicReference<String>  whileDoingDescString = new AtomicReference<>("");
        AtomicReference<String>  whileDoingActivityTypeString = new AtomicReference<>("");

        Random rand = new Random();
        TextField nameText = new TextField();
        nameText.setColumns(15);
        TextField surnameText = new TextField();
        surnameText.setColumns(15);
        JButton whileDoing = new JButton("while doing");
        BidiMap<String,String> activityType = CM.getActivityType();
        whileDoing.addActionListener( e -> {
            NameDesc tmp = addNewActivityTuple("Insert your activity while listening",activityType);
            whileDoingNameString.set(tmp.name);
            whileDoingDescString.set(tmp.desc);
            whileDoingActivityTypeString.set(tmp.activityType);
        });
        JButton wantToDo = new JButton("want to do");
        wantToDo.addActionListener( e -> {
            NameDesc tmp = addNewActivityTuple("Insert the activity this listening inspires you to do",activityType);
            toDoNameString.set(tmp.name);
            toDoDescString.set(tmp.desc);
            toDoActivityTypeString.set(tmp.activityType);
        });
        JButton memories = new JButton("revives memory");
        memories.addActionListener( e -> {
            NameDesc tmp = addNewTuple();
            memoryNameString.set(tmp.name);
            memoryDescString.set(tmp.desc);
        });
        JButton location = new JButton("location");
        JComboBox<String> locations = new JComboBox<>();
        locations.addItem("");
        CM.getLocations().forEach((key, value) -> locations.addItem(key + "-" + value));
        location.addActionListener( e -> {
            NameDesc tmp = addNewLocationTuple(locations);
            locationNameString.set(tmp.name);
            locationDescString.set(tmp.desc);
            oldLocation.set(tmp.activityType);
        });

        JComboBox<String> songsCombo = new JComboBox<>();
        BidiMap<String,String> songs =  CM.getSongs();
        songs.forEach((key,value) -> songsCombo.addItem(value));

        JButton insert = new JButton("Insert");
        insert.addActionListener( e ->{
            String name = nameText.getText();
            String surname = surnameText.getText();
            if(!name.equals("") && !surname.equals("")){
                CM.addQuery(name,surname,String.valueOf(songs.getKey(songsCombo.getSelectedItem())),
                        whileDoingNameString.get(),whileDoingDescString.get(),whileDoingActivityTypeString.get(),
                        toDoNameString.get(), toDoDescString.get(),toDoActivityTypeString.get(),
                        memoryNameString.get(), memoryDescString.get(),
                        locationNameString.get(),locationDescString.get(),oldLocation.get(),
                        preImagesButtonGroup.getSelection().getActionCommand(),
                        postImagesButtonGroup.getSelection().getActionCommand());
            }
        });
        List<String> images = CM.getImages();
        for(int i=0;i<=2;i++){
            String imgPre = images.get(rand.nextInt(images.size()))+".jpg";
            MyButton pre = new MyButton(imgPre,getIcon(imgPre));
            pre.setActionCommand(imgPre.substring(0,imgPre.lastIndexOf(".")));
            pre.setSize(SCREEN_SIZE.width/6,SCREEN_SIZE.height/6);
            String imgPost = images.get(rand.nextInt(images.size()))+".jpg";
            MyButton post = new MyButton(imgPost,getIcon(imgPost));
            post.setActionCommand(imgPost.substring(0,imgPost.lastIndexOf(".")));
            preImagesButtonGroup.add(pre);
            postImagesButtonGroup.add(post);
            preImagesPanel.add(pre);
            postImagesPanel.add(post);
            preImagesButtonGroup.setSelected(pre.getModel(),true);
            postImagesButtonGroup.setSelected(post.getModel(),true);
        }

        JFrame frame = new JFrame("InMusic-App");
        JPanel bigPane = new JPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(SCREEN_SIZE.width/2,SCREEN_SIZE.height/2);

        bigPane.setLayout(new BoxLayout(bigPane,BoxLayout.PAGE_AXIS));
        preImagesPanel.setLayout(new FlowLayout());
        songsPanel.setLayout(new FlowLayout());
        postImagesPanel.setLayout(new FlowLayout());

        songsPanel.add(new Label("Name:"));
        songsPanel.add(nameText);
        songsPanel.add(new Label( "Surname:"));
        songsPanel.add(surnameText);
        songsPanel.add(songsCombo);
        songsPanel.add(whileDoing);
        songsPanel.add(wantToDo);
        songsPanel.add(location);
        songsPanel.add(memories);

        bigPane.add(new Label("Select the image that most represents your mood before listening to such song"));
        bigPane.add(preImagesPanel);
        bigPane.add(songsPanel);
        bigPane.add(new Label("Select the image that most represents your mood after listening to such song"));
        bigPane.add(postImagesPanel);
        bigPane.add(insert);

        menu.add("Populate DB",bigPane);
        frame.setContentPane(menu);
        frame.pack();
        frame.setVisible(true);
    }

    private static StretchIcon getIcon(String name) throws IOException {
        return new StretchIcon(ImageIO.read(Objects.requireNonNull(InMusicApp.class.getClassLoader().getResourceAsStream(name))));
    }

    private static class MyButton extends JToggleButton{
        String name;
        public MyButton(String name, StretchIcon img) {
            super(img);
            this.name = name;
            this.setPreferredSize(new Dimension(300, 300));
            this.setBackground(Color.BLACK);
            this.setBorder(BorderFactory.createEmptyBorder());
        }
        @Override
        public String toString() {
            return name;
        }
    }

    private static NameDesc addNewTuple(){
        JTextField someName = new JTextField();
        JTextField someDesc = new JTextField();
        JComponent[] inputs = new JComponent[] {
                new JLabel("Name"),
                someName,
                new JLabel("Description"),
                someDesc,
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Insert the memory revived with this listening", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new NameDesc(someName.getText(),someDesc.getText());
        } else {
            return new NameDesc("","");
        }

    }
    private static NameDesc addNewActivityTuple(final String name, final BidiMap<String,String> activityType){
        JTextField someName = new JTextField();
        JTextField someDesc = new JTextField();
        JComboBox<String> activities = new JComboBox<>();
        activityType.values().forEach(activities::addItem);
        JComponent[] inputs = new JComponent[] {
                new JLabel ("Activity Type:"),
                activities,
                new JLabel("Name"),
                someName,
                new JLabel("Description"),
                someDesc,
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, name, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return new NameDesc(someName.getText(),someDesc.getText(),activityType.getKey(activities.getSelectedItem()));
        } else {
            return new NameDesc("","");
        }

    }
    private static NameDesc addNewLocationTuple(final JComboBox<String> oldLocations){
        JTextField someName = new JTextField();
        JTextField someDesc = new JTextField();
        JComponent[] inputs = new JComponent[] {
                new JLabel ("Locations:"),
                oldLocations,
                new JLabel("Name"),
                someName,
                new JLabel("Description"),
                someDesc,
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "Insert the location where this listening took place", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if(String.valueOf(oldLocations.getSelectedItem()).equals("")){
                return new NameDesc(someName.getText(),someDesc.getText());
            }else{
                return new NameDesc("","",String.valueOf(oldLocations.getSelectedItem()));
            }
        } else {
            return new NameDesc("","");
        }

    }
    private static class NameDesc {
        String name;
        String desc;
        String activityType;

        public NameDesc(final String text,final String text1) {
            this.name = text;
            this.desc = text1;
        }
        public NameDesc(final String text,final String text1,final String activityType) {
            this.name = text;
            this.desc = text1;
            this.activityType = activityType;
        }
    }

}


