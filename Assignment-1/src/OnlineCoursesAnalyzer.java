import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {
    private List<Course> courseList;

    public void ha() {
        List<String> list = new ArrayList<>();
        courseList.forEach(course -> {
            String[] instr = course.getInstructors().split(", ");
            for (String ins : instr) {
                if (!list.contains(ins)) {
                    list.add(ins);
                }
            }
        });
        list.sort(String::compareTo);
        System.out.println(Arrays.toString(list.toArray()));
    }

    public OnlineCoursesAnalyzer(String datasetPath) {
        this.courseList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(datasetPath),
                StandardCharsets.UTF_8)) {
            String line;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                Course course = new Course(
                        columns[0], columns[1],
                        new Date(columns[2]), columns[3], columns[4], columns[5],
                        Integer.parseInt(columns[6]), Integer.parseInt(columns[7]),
                        Integer.parseInt(columns[8]), Integer.parseInt(columns[9]),
                        Integer.parseInt(columns[10]), Double.parseDouble(columns[11]),
                        Double.parseDouble(columns[12]), Double.parseDouble(columns[13]),
                        Double.parseDouble(columns[14]), Double.parseDouble(columns[15]),
                        Double.parseDouble(columns[16]), Double.parseDouble(columns[17]),
                        Double.parseDouble(columns[18]), Double.parseDouble(columns[19]),
                        Double.parseDouble(columns[20]), Double.parseDouble(columns[21]),
                        Double.parseDouble(columns[22]));
                courseList.add(course);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        return courseList.stream().collect(
                Collectors.groupingBy(Course::getInstitution,
                        TreeMap::new, Collectors.summingInt(Course::getParticipants)));
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> res = new LinkedHashMap<>();
        Map<String, Integer> ret = courseList.stream().collect(
                Collectors.groupingBy(
                        Course::getInstAndSubject,
                        TreeMap::new, Collectors.summingInt(Course::getParticipants)));
        ret.entrySet().stream().sorted(((o1, o2) -> {
            int result = o2.getValue().compareTo(o1.getValue());
            if (result == 0) {
                return o1.getKey().compareTo(o2.getKey());
            } else {
                return result;
            }
        })).forEach(e -> res.put(e.getKey(), e.getValue()));
        return res;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> res = new HashMap<>();
        List<String> independent = new ArrayList<>();
        List<String> coDevelop = new ArrayList<>();
        courseList.forEach(course -> {
            String[] instr = course.getInstructors().split(", ");
            for (String ins : instr) {
                if (!res.containsKey(ins)) {
                    List<List<String>> tmp = new ArrayList<>();
                    List<String> t1 = new ArrayList<>();
                    List<String> t2 = new ArrayList<>();
                    tmp.add(t1);
                    tmp.add(t2);
                    res.put(ins, tmp);
                }
            }
        });
        courseList.stream().filter(course -> {
            String[] instr = course.getInstructors().split(", ");
            return instr.length == 1;
        }).forEach(course -> {
            List<String> t = res.get(course.getInstructors()).get(0);
            if (!t.contains(course.getTitle())) {
                t.add(course.getTitle());
            }
        });
        courseList.stream().filter(course -> {
            String[] instr = course.getInstructors().split(", ");
            return instr.length != 1;
        }).forEach(course -> {
            String[] instr = course.getInstructors().split(", ");
            for (String ins : instr) {
                List<String> t = res.get(ins).get(1);
                if (!t.contains(course.getTitle())) {
                    t.add(course.getTitle());
                }
            }
        });
        res.forEach((s, lists) -> {
            lists.get(0).sort(String::compareTo);
            lists.get(1).sort(String::compareTo);
        });
        return res;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<String> res = new ArrayList<>();
        List<Course> tmp = new ArrayList<>(by.equals("hours")
                ? courseList.stream().sorted((o1, o2) -> {
            Double d1 = o1.getTotalHours();
            Double d2 = o2.getTotalHours();
            return d2.compareTo(d1);
        }).toList() :
                courseList.stream().sorted((o1, o2) -> {
                    Integer d1 = o1.getParticipants();
                    Integer d2 = o2.getParticipants();
                    return d2.compareTo(d1);
                }).toList());
        if (by.equals("hours")) {
            tmp.sort((o1, o2) -> {
                Double d1 = o1.getTotalHours();
                Double d2 = o2.getTotalHours();
                return d2.compareTo(d1) != 0 ? d2.compareTo(d1) : o1.getTitle().compareTo(o2.getTitle());
            });
        } else {
            tmp.sort((o1, o2) -> {
                Integer d1 = o1.getParticipants();
                Integer d2 = o2.getParticipants();
                return d2.compareTo(d1) != 0 ? d2.compareTo(d1) : o1.getTitle().compareTo(o2.getTitle());
            });
        }
        int k = 0;
        int idx = -1;
        while (k < topK) {
            idx++;
            if (!res.contains(tmp.get(idx).getTitle())) {
                k++;
                res.add(tmp.get(idx).getTitle());
            }
        }
        return res;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> res = new ArrayList<>();
        courseList.stream()
                .filter(course -> course.getSubject().toLowerCase().contains(courseSubject.toLowerCase()))
                .filter(course -> course.getPercentAudited() >= percentAudited)
                .filter(course -> course.getTotalHours() <= totalCourseHours)
                .forEach(course -> {
                    if (!res.contains(course.getTitle())) {
                        res.add(course.getTitle());
                    }
                });
        res.sort(String::compareTo);
        return res;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        List<String> res = new ArrayList<>();
        Map<String, Double> numberTitleVal = new TreeMap<>();
        Map<String, Double> numberAge;
        Map<String, Double> numberMale;
        Map<String, Double> numberBachelor;
        Map<String, String> numberTitle = new HashMap<>();
        Map<String, List<Course>> numberCourse;
        numberCourse = courseList.stream().collect(Collectors.groupingBy(Course::getNumber));
        numberCourse.forEach((s, courses) -> {
            courses.sort((o1, o2) -> o2.getLaunchDate().compareTo(o1.getLaunchDate()));
        });
        numberCourse.forEach((s, courses) -> {
            numberTitle.put(s, courses.get(0).getTitle());
        });
        numberAge = courseList.stream().collect(
                Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getMedianAge)));
        numberMale = courseList.stream().collect(
                Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentMale)));
        numberBachelor = courseList.stream().collect(
                Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentBachelor)));
        Map<String, Double> finalNumberAge = numberAge;
        Map<String, Double> finalNumberMale = numberMale;
        Map<String, Double> finalNumberBachelor = numberBachelor;
        numberCourse.keySet().forEach(s -> {
            numberTitleVal.put(numberTitle.get(s),
                    Math.pow(age - finalNumberAge.get(s), 2)
                            + Math.pow(gender * 100 - finalNumberMale.get(s), 2)
                            + Math.pow(isBachelorOrHigher * 100 - finalNumberBachelor.get(s), 2)
            );
        });
        List<String> finalRes = res;
        numberTitleVal.entrySet().stream().sorted((o1, o2) -> {
            int result = o1.getValue().compareTo(o2.getValue());
            if (result == 0) {
                return o1.getKey().compareTo(o2.getKey());
            } else {
                return result;
            }
        }).forEach(stringDoubleEntry -> {
            if (!finalRes.contains(stringDoubleEntry.getKey())) {
                finalRes.add(stringDoubleEntry.getKey());
            }
        });
        res = finalRes.stream().limit(10).toList();
        return res;
    }
}

class Course {
    private String institution;
    private String number;
    private Date launchDate;
    private String title;
    private String instructors;
    private String subject;
    private int year;
    private int honorCode;
    private int participants;
    private int audited;
    private int certified;
    private double percentAudited;
    private double percentCertified;
    private double percentCertified50;
    private double percentVideo;
    private double percentForum;
    private double gradeHigherZero;
    private double totalHours;
    private double medianHoursCertification;
    private double medianAge;
    private double percentMale;
    private double percentFemale;
    //    private double percentDegree;
    private double percentBachelor;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentBachelor) {

        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
//        this.percentDegree = percentDegree;
        this.percentBachelor = percentBachelor;
    }

    public String getInstAndSubject() {
        return institution + "-" + subject;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructors() {
        return instructors;
    }

    public void setInstructors(String instructors) {
        this.instructors = instructors;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public void setHonorCode(int honorCode) {
        this.honorCode = honorCode;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public int getAudited() {
        return audited;
    }

    public void setAudited(int audited) {
        this.audited = audited;
    }

    public int getCertified() {
        return certified;
    }

    public void setCertified(int certified) {
        this.certified = certified;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public void setPercentAudited(double percentAudited) {
        this.percentAudited = percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public void setPercentCertified(double percentCertified) {
        this.percentCertified = percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public void setPercentCertified50(double percentCertified50) {
        this.percentCertified50 = percentCertified50;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public void setPercentVideo(double percentVideo) {
        this.percentVideo = percentVideo;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public void setPercentForum(double percentForum) {
        this.percentForum = percentForum;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public void setGradeHigherZero(double gradeHigherZero) {
        this.gradeHigherZero = gradeHigherZero;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public void setMedianHoursCertification(double medianHoursCertification) {
        this.medianHoursCertification = medianHoursCertification;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public void setMedianAge(double medianAge) {
        this.medianAge = medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public void setPercentMale(double percentMale) {
        this.percentMale = percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public void setPercentFemale(double percentFemale) {
        this.percentFemale = percentFemale;
    }

//    public double getPercentDegree() {
//        return percentDegree;
//    }
//
//    public void setPercentDegree(double percentDegree) {
//        this.percentDegree = percentDegree;
//    }

    public double getPercentBachelor() {
        return percentBachelor;
    }

    public void setPercentBachelor(double percentBachelor) {
        this.percentBachelor = percentBachelor;
    }
}
